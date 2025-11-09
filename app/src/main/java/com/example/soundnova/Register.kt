package com.example.soundnova

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.soundnova.databinding.SignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class Register : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: SignUpBinding
    private lateinit var validator: Validator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = SignUpBinding.bind(view)
        firebaseAuth = FirebaseAuth.getInstance()
        validator = Validator()
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextSignUpEmail.text.toString()
            val password = binding.editTextSignUpPassword.text.toString()
            val name = binding.editTextSignUpUsername.text.toString()
            val repeatpassword = binding.editTextSignUpRepeatPassword.text.toString()

            val emailStatus = validator.checkEmail(email)
            val passwordStatus = validator.checkPassword(password)
            val repeatStatus = validator.checkRepeatPassword(password, repeatpassword)

            if (emailStatus == "valid" && passwordStatus == "valid" && repeatStatus == "correct") {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }
                            user!!.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User profile updated.")
                                    }
                                }

                            val intent = Intent(requireContext(), Login::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            } else {
                val message = when {
                    emailStatus == "empty" -> "Email is empty"
                    emailStatus == "invalid" -> "Invalid email format"
                    passwordStatus == "empty" -> "Password is empty"
                    passwordStatus == "invalid" -> "Invalid password format"
                    repeatStatus == "empty" -> "repeatpassword is empty"
                    repeatStatus == "incorrect" -> "repeatpassword is incorrect"
                    else -> "Unknown error"
                }
                binding.note.text = "$message"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewSignIn.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (firebaseAuth.currentUser != null) {
            currentUser?.reload()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Log.w("", "Failed to reload user", task.exception)
                }
            }
        }
    }
}