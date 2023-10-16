package com.example.taskwhiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RestorePasswordActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var sendResetLinkButton: Button
    private lateinit var logInLinkButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore_password)

        emailEditText = findViewById(R.id.forgotPasswordEmailEditText)
        sendResetLinkButton = findViewById(R.id.sendResetLinkButton)
        logInLinkButton = findViewById(R.id.loginLink)

        val auth = FirebaseAuth.getInstance()

        sendResetLinkButton.setOnClickListener {
            val email = emailEditText.text.toString()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Se ha enviado un enlace de restablecimiento a su correo electrónico.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al enviar el enlace de restablecimiento. Verifique la dirección de correo electrónico.", Toast.LENGTH_SHORT).show()
                    }
                    showLogIn(email)
                }
        }

        logInLinkButton.setOnClickListener {
            val email = emailEditText.text.toString()
            showLogIn(email)
        }


    }

    private fun showLogIn(email: String) {
        val logInIntent = Intent(this, AuthActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(logInIntent)
    }
}