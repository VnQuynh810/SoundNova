package com.example.soundnova

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.soundnova.databinding.ForgetPasswordActivityBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class ForgotPassword : Fragment() {
    private lateinit var binding: ForgetPasswordActivityBinding
    private lateinit var validator : Validator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ForgetPasswordActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ForgetPasswordActivityBinding.bind(view)
        validator = Validator()

        binding.buttonBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.buttonSendBTN.setOnClickListener {
            val email = binding.editTextSignUpEmail.text.toString()
            val emailStatus = validator.checkEmail(email)
            if (email.isEmpty()) {
                binding.note.text = "Email is empty"
            } else if (emailStatus == "valid"){
                // gui thong bao ve email
                Firebase.auth.sendPasswordResetEmail(email)
            } else
                binding.note.text = "Email is $emailStatus"
        }
    }
}
