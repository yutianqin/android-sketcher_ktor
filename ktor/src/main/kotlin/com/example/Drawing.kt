package com.example

import org.jetbrains.exposed.dao.id.IntIdTable

object DrawingTable : IntIdTable()
{
    val drawingTitle = varchar("drawingTitle", 255)
    val imagePath = varchar("imagePath", 255).nullable()
    val user_uid = varchar("user_uid", 28)
}
