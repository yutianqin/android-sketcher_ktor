package com.example.androidproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    private val drawingViewModel: DrawingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        val navController = findNavController()
        var user by remember { mutableStateOf(Firebase.auth.currentUser) }

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        var errorMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user == null) {
                // User is not logged in
                Text("Not logged in")

                // Email input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )

                // Password input with visual transformation for hiding password text
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                // Show error message if present
                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Login button
                    Button(onClick = {
                        // Attempt to login
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sign-in success, update UI with the signed-in user's information
                                    user = Firebase.auth.currentUser
                                    errorMessage = "" // clear error message
                                } else {
                                    // If sign-in fails, display a message to the user.
                                    errorMessage = "Authentication failed."
                                }
                            }
                    }) {
                        Text("Log In")
                    }

                    Spacer(modifier = Modifier.width(8.dp)) // Add some spacing between the buttons

                    // Sign-up button
                    Button(onClick = {
                        // Attempt to create a new user
                        Firebase.auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Sign-up success, update UI with the signed-in user's information
                                    user = Firebase.auth.currentUser
                                    errorMessage = "" // clear error message
                                } else {
                                    // If sign-up fails, display a message to the user.
                                    errorMessage = "Failed to create a user."
                                }
                            }
                    }) {
                        Text("Sign Up")
                    }
                }
            } else {
                navController.navigate(R.id.action_loginFragment_to_startFragment)
                drawingViewModel.currentUser = email
            }
        }
    }
}