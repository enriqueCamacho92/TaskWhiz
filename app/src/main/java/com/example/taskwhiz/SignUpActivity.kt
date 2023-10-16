package com.example.taskwhiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.taskwhiz.databinding.ActivityAuthBinding
import com.example.taskwhiz.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

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
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty() &&
                binding.confirmPasswordEditText.text.toString().equals(binding.passwordEditText.text.toString())) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailEditText.text.toString(),binding.passwordEditText.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this, "La cuenta se ha registrado con éxito!", Toast.LENGTH_SHORT).show()
                            showLogIn(it.result?.user?.email ?:"")
                        }else{
                            showAlert()
                        }
                    }
            } else {
                showAlert()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Revisa que los datos sean válidos")
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