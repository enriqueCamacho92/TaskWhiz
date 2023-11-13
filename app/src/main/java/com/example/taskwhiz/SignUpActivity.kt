package com.example.taskwhiz

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.taskwhiz.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setup()
    }

    private fun setup() {
        binding.registerButton.setOnClickListener{
            if (binding.emailEditText.text.isNotEmpty() &&
                binding.passwordEditText.text.isNotEmpty() &&
                binding.confirmPasswordEditText.text.toString() == binding.passwordEditText.text.toString()
            ) {
                val auth = FirebaseAuth.getInstance()
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()
                val nombre = binding.usernameEditText.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Registro exitoso
                            Toast.makeText(this, "La cuenta se ha registrado con éxito!", Toast.LENGTH_SHORT).show()

                            // Guardar el nombre en Firestore
                            saveNombreToFirestore(auth.currentUser?.uid?: "", nombre, email, password)

                            // Redirigir al inicio de sesión
                            showLogIn(auth.currentUser?.email ?: "")
                        } else {
                            val errorMessage = task.exception?.message ?: "Error desconocido"
                            showAlert(errorMessage)
                        }
                    }
            } else {
                showAlert("Las contraseñas no coinciden")
            }
        }
    }

    private fun saveNombreToFirestore(usuarioId: String, nombre: String, email: String, password: String) {
        // Referencia a la colección "usuarios" en Firestore
        val usuariosCollection = FirebaseFirestore.getInstance().collection("usuarios")

        // Crear un documento con el ID del usuario actual
        val usuarioDocument = usuariosCollection.document(usuarioId)

        // Actualizar el campo "nombre" en el documento del usuario
        usuarioDocument.set( hashMapOf(
            "id" to usuarioId,
            "nombre" to nombre,
            "email" to email,
            "password" to password)
        ).addOnSuccessListener {
                // Éxito al guardar el nombre en Firestore
                Log.d(TAG, "Nombre guardado en Firestore: $nombre")
            }
            .addOnFailureListener { e ->
                // Error al guardar el nombre en Firestore
                Log.e(TAG, "Error al guardar el nombre en Firestore", e)
            }
    }

    private fun showAlert(errorMessage: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(errorMessage)
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showLogIn(email: String) {
        val logInIntent = Intent(this, AuthActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(logInIntent)
    }
}