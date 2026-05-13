package com.suraksha.setu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // If already registered & logged in, skip to main
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val nameField     = findViewById<EditText>(R.id.etRegName)
        val phoneField    = findViewById<EditText>(R.id.etRegPhone)
        val emailField    = findViewById<EditText>(R.id.etRegEmail)
        val passwordField = findViewById<EditText>(R.id.etRegPassword)
        val registerBtn   = findViewById<Button>(R.id.btnRegister)
        val loginLink     = findViewById<TextView>(R.id.tvBackToLogin)

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        registerBtn.setOnClickListener {
            val name     = nameField.text.toString().trim()
            val phone    = phoneField.text.toString().trim()
            val email    = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerBtn.isEnabled = false
            registerBtn.text = "Creating account..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser!!
                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build()
                        user.updateProfile(profileUpdate)

                        val userData = hashMapOf(
                            "uid"         to user.uid,
                            "name"        to name,
                            "phone"       to phone,
                            "email"       to email,
                            "isVolunteer" to false,
                            "createdAt"   to System.currentTimeMillis()
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnCompleteListener {
                                Toast.makeText(this, "Account created! Welcome to Suraksha-Setu", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                    } else {
                        registerBtn.isEnabled = true
                        registerBtn.text = "CREATE ACCOUNT"
                        Toast.makeText(this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
