package com.example.soundnova

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.soundnova.databinding.SettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import java.io.File
import java.io.FileOutputStream


class SettingsFragment: Fragment() {
    private lateinit var binding: SettingBinding
    var currentUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = SettingBinding.bind(view)
        // Sử dụng binding để thao tác với View
        val toggleSaveVisibility: (Boolean) -> Unit = { isVisible ->
            binding.changeProfile.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        binding.userName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) toggleSaveVisibility(true)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {

            val name = currentUser.displayName
            val imageUri = currentUser.photoUrl
            if (!name.isNullOrEmpty()) {
                binding.userName.setText(name)
            }
            if (imageUri != null) {
                loadAvatar()
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }

        binding.changeProfile.setOnClickListener {
            binding.userName.clearFocus()
            loadAvatar()
            if (currentUser != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = binding.userName.text.toString()
                    if (currentUri != null) {
                        photoUri = currentUri
                    }
                }

                currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.userName.setText(currentUser.displayName)
                        }
                    }
            }
            toggleSaveVisibility(false)
        }

        binding.profileImage.setOnClickListener {
            openGallery()
            toggleSaveVisibility(true)
        }

        binding.logOut.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.changePasswordRow.setOnClickListener {
            findNavController().navigate(R.id.changePassword)
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Hiển thị ảnh trong ImageView
            saveImageToInternalStorage(uri)
            currentUri = uri
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
    // Lưu ảnh từ URI vào bộ nhớ cục bộ
    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "avatar.jpg") // Lưu ảnh với tên "avatar.jpg" trong internal storage
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Toast.makeText(requireContext(), "Avatar updated successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save avatar", Toast.LENGTH_SHORT).show()
        }
    }

    // Tải ảnh avatar từ bộ nhớ cục bộ và hiển thị
    private fun loadAvatar() {
        val file = File(requireContext().filesDir, "avatar.jpg")
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            binding.profileImage.setImageBitmap(bitmap)
        } else {
            Toast.makeText(requireContext(), "No avatar saved", Toast.LENGTH_SHORT).show()
        }
    }
}