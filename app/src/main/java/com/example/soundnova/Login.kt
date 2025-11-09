package com.example.soundnova

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonConfirmLogin: Button
    private lateinit var textViewForgotPassword: TextView
    private lateinit var textViewSignUp: TextView
    private lateinit var note: TextView
    var count = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)
        firebaseAuth = FirebaseAuth.getInstance()

        editTextEmail = findViewById(R.id.editTextSignInUsername)
        editTextPassword = findViewById(R.id.editTextSignInPassword)
        buttonConfirmLogin = findViewById(R.id.buttonConfirmLogin)
        textViewForgotPassword = findViewById(R.id.textViewForgetPassword)
        textViewSignUp = findViewById(R.id.textViewSignUp)
        note = findViewById(R.id.note)

        buttonConfirmLogin.setOnClickListener {
            var email : String? = null
            var password : String? = null
            email = editTextEmail.text.toString()
            password = editTextPassword.text.toString()
//            if (email.isNotEmpty() && password.isNotEmpty()) {
//                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        val intent = Intent(applicationContext, HomeActivity::class.java)
//                        startActivity(intent)
//                    } else {
//                        note.setText("Email or Password is not correct")
//                        Toast.makeText(this,  "Email or Password is not correct", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//            else if (email.isEmpty()) {
//                note.setText("Email is empty")
//
//            }
//            else note.setText("Password is empty")

            if (email.isEmpty()) {
                note.text = "Email is empty"
            }
            else if (password.isEmpty()) {
                note.text = "Password is empty"
            }
            else {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        count = 1
                        val intent = Intent(applicationContext, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        note.text = "Email or Password is not correct"
                        Toast.makeText(this,  "Email or Password is not correct", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        textViewSignUp.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, Register())
                .addToBackStack(null)
                .commit()
        }

        textViewForgotPassword.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, ForgotPassword())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (firebaseAuth.currentUser != null) {
            currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("", "Failed to reload user", task.exception)
                }
            }
        }
    }

}