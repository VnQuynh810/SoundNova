package com.example.soundnova

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.soundnova.databinding.ChangePasswordBinding
import com.example.soundnova.databinding.SearchBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePassword : Fragment() {
    private lateinit var binding: ChangePasswordBinding
    private lateinit var validator: Validator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ChangePasswordBinding.bind(view)
        validator = Validator()

        binding.buttonCancel.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.buttonSendChange.setOnClickListener {
            val oldPassword = binding.editTextCurrentPass.text.toString()
            val newPassword = binding.editTextNewPass.text.toString()
            val confirmNewPassword = binding.editTextConfirmNewPass.text.toString()
            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email
            if(email != null) {
                val credential = EmailAuthProvider.getCredential(email, oldPassword)

                user?.reauthenticate(credential)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (newPassword == confirmNewPassword
                                && validator.checkPassword(newPassword) == "valid") {
                                user?.updatePassword(newPassword)
                                    ?.addOnCompleteListener() { task ->
                                        if(task.isSuccessful) {
                                            val intent = Intent(requireContext(), HomeActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finish()
                                        } else {
                                            binding.note.text = "Password is not updated"
                                        }
                                    }
                            } else {
                                if (newPassword == oldPassword) {
                                    binding.note.text = "New password is similar to old password"
                                }
                                binding.note.text = "New password is not correct"
                            }
                        } else {
                            binding.note.text = "Password is not correct"
                        }
                    }
            } else {
                binding.note.text = "user state is not saved"
            }
        }
    }
}