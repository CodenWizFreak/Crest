package com.example.musicstream

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.musicstream.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mAuth = FirebaseAuth.getInstance()


        val sendEmailBtn = findViewById<Button>(R.id.sendEmailBtn)
        sendEmailBtn.setOnClickListener{

        }
        binding.sendEmailBtn.setOnClickListener {
            val email = binding.emailEdittext.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendPasswordResetEmail(email)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        // Show progress bar
        binding.progressBar.visibility = ProgressBar.VISIBLE
        binding.sendEmailBtn.isEnabled = false // Disable the button to prevent multiple clicks

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Hide progress bar once the task is complete
                binding.progressBar.visibility = ProgressBar.GONE
                binding.sendEmailBtn.isEnabled = true // Enable the button again

                if (task.isSuccessful) {
                    // Success, show a message
                    Toast.makeText(this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                } else {
                    // Error, show the error message
                    val errorMessage = task.exception?.message ?: "An error occurred. Please try again."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}