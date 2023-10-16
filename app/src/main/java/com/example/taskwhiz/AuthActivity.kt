package com.example.taskwhiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.taskwhiz.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        setup(email ?: "")
    }

    private fun setup(email:String) {
        title = "Inicia Sesión"
        binding.emailEditText.setText(email)

        binding.loginButton.setOnClickListener{
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.emailEditText.text.toString(),binding.passwordEditText.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this, "ha iniciado sesión!", Toast.LENGTH_SHORT).show()
                        }else{
                            showAlert()
                        }
                    }
            }
        }

        binding.registerLink.setOnClickListener{
            showSignUp()
        }

        binding.forgotPasswordLink.setOnClickListener {
            showRestorePassword()
        }
    }

    private fun showRestorePassword() {
        val restorepaswordIntent = Intent(this, RestorePasswordActivity::class.java)
        startActivity(restorepaswordIntent)
    }

    private fun showSignUp() {
        val signUpIntent = Intent(this, SignUpActivity::class.java)
        startActivity(signUpIntent)
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}