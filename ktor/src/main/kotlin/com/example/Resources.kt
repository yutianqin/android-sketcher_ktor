package com.example

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.get

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File


@Serializable
data class Drawings(
    val drawingTitle: String,
    val imagePath: String?,
    val user_uid: String?,
    val isUser: Boolean
)

fun Application.configureResources() {
    routing {
        route("/drawings") {
            // To fetch shared images by all users
            get("/shared") {
                // Reading the header
                val userHeader: String? = call.request.authorization()
                try {
                    val userAuth = FirebaseAuth.getInstance().getUser(userHeader.toString())
                    val drawings = newSuspendedTransaction(Dispatchers.IO) {
                        val drawings = DrawingTable.selectAll()
                        drawings.map {
                            Drawings(
                                it[DrawingTable.drawingTitle],
                                it[DrawingTable.imagePath],
                                it[DrawingTable.user_uid],
                                it[DrawingTable.user_uid] == userAuth.uid
                            )
                        }
                    }

                    if (drawings.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound, "Drawings not found")
                        return@get
                    }

                    println("Total drawings from DB: ${drawings.count()}")
                    println("drawings: $drawings")

                    call.respond(drawings)

                } catch ( e : FirebaseAuthException) {
                    call.respondText { "Unauthorized Request" }
                }
            }

            get("{filename}") {
                val filename = call.parameters["filename"]
                if (filename != null) {
                    val file = File("src/main/resources/images/$filename")
                    println("File: $filename")
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Filename not provided")
                }
            }

            // For user to POST an image to this server and store it in h2-database
            post {
                val multipart = call.receiveMultipart()
                var drawingTitle: String? = null
                var imagePath: String? = null
                val imageDirectory = "src/main/resources/images"
                var uid: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "drawingTitle") {
                                drawingTitle = part.value
                            }
                            if (part.name == "userUID") {
                                uid = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            val fileName = "${drawingTitle}.png"
                            imagePath = "$imageDirectory/$fileName"
                            part.streamProvider().use { input ->
                                val fileData = input.readBytes()
                                imagePath?.let { File(it).writeBytes(fileData) }
                            }
                        }
                        else -> {
                            // Do nothing
                        }
                    }
                    part.dispose()
                }

                if (drawingTitle != null && imagePath != null) {
                    val id = newSuspendedTransaction(Dispatchers.IO) {
                        DrawingTable.insertAndGetId {
                            it[this.drawingTitle] = drawingTitle!!
                            it[this.imagePath] = imagePath
                            it[this.user_uid] = uid!!
                        }
                    }
                    call.respond(HttpStatusCode.Created, "Drawing created with ID $id")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Missing drawing title or image")
                }

                call.respondText("$drawingTitle is uploaded")
            }


            delete("/{drawingTitle}") {
                val drawingTitle = call.parameters["drawingTitle"]
                if (!drawingTitle.isNullOrBlank()) {
                    newSuspendedTransaction(Dispatchers.IO) {
                        val imagePath = DrawingTable.select { DrawingTable.drawingTitle eq drawingTitle }
                            .map { it[DrawingTable.imagePath] }
                            .singleOrNull()

                        val deletedCount = DrawingTable.deleteWhere { DrawingTable.drawingTitle eq drawingTitle }

                        if (deletedCount > 0) {
                            // Delete the associated image file
                            imagePath?.let {
                                val file = File(it)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "No drawing with title $drawingTitle found")
                        }
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Invalid drawingTitle parameter")
                }
            }
        }

    }
}