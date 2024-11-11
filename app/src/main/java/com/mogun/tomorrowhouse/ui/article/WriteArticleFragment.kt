package com.mogun.tomorrowhouse.ui.article

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.mogun.tomorrowhouse.R
import com.mogun.tomorrowhouse.databinding.FragmentWriteBinding
import java.util.UUID

class WriteArticleFragment : Fragment(R.layout.fragment_write) {

    private lateinit var binding: FragmentWriteBinding
    private var selectedUri: Uri? = null

    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            selectedUri = uri
            binding.photoImageView.setImageURI(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteBinding.bind(view)

        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))

        binding.photoImageView.setOnClickListener {

        }

        binding.deleteButton.setOnClickListener {

        }

        binding.submitButton.setOnClickListener {
            if(selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                val fileName = "${UUID.randomUUID()}.png"
                Firebase.storage.reference.child("articles/photo").child(fileName)
                    .putFile(photoUri)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Firebase.storage.reference.child("articles/photo/$fileName").downloadUrl
                                .addOnSuccessListener {

                                }.addOnFailureListener {

                                }

                            Snackbar.make(view, "게시글 작성이 완료되었습니다.", Snackbar.LENGTH_SHORT).show()
                        } else {

                        }
                    }
            } else {
                Snackbar.make(view, "사진을 선택해주세요.", Snackbar.LENGTH_SHORT).show()
            }

        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(WriteArticleFragmentDirections.actionBack())
        }

    }
}