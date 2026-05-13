package com.suraksha.setu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // If already logged in, go straight to Main
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val emailField    = findViewById<EditText>(R.id.etLoginEmail)
        val passwordField = findViewById<EditText>(R.id.etLoginPassword)
        val loginButton   = findViewById<Button>(R.id.btnLogin)
        val registerLink  = findViewById<TextView>(R.id.tvGoToRegister)

        loginButton.setOnClickListener {
            val email    = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            loginButton.text = "Logging in..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    loginButton.isEnabled = true
                    loginButton.text = "LOGIN"
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
