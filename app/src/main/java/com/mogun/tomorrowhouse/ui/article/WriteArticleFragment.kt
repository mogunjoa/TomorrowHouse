package com.mogun.tomorrowhouse.ui.article

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.mogun.tomorrowhouse.R
import com.mogun.tomorrowhouse.data.ArticleModel
import com.mogun.tomorrowhouse.databinding.FragmentWriteBinding
import java.util.UUID

class WriteArticleFragment : Fragment(R.layout.fragment_write) {

    private lateinit var binding: FragmentWriteBinding
    private var selectedUri: Uri? = null

    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            binding.photoImageView.setImageURI(uri)
            binding.plusButton.isVisible = false
            binding.deleteButton.isVisible = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteBinding.bind(view)

        startPicker()
        setupPhotoImageView()
        setupDelete()
        setupSubmitButton(view)
        setupBackButton()
    }

    private fun setupSubmitButton(view: View) {
        binding.submitButton.setOnClickListener {
            showProgress()
            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadImage(photoUri,
                    successHandler = { uri ->
                        uploadArticle(uri, binding.descriptionEditText.text.toString())
                    },
                    errorHandler = {
                        Snackbar.make(view, "사진 업로드에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            } else {
                Snackbar.make(view, "사진을 선택해주세요.", Snackbar.LENGTH_SHORT).show()
                hideProgress()
            }

        }
    }

    private fun setupDelete() {
        binding.deleteButton.setOnClickListener {
            binding.photoImageView.setImageURI(null)
            selectedUri = null
            binding.deleteButton.isVisible = false
            binding.plusButton.isVisible = true
        }
    }

    private fun setupPhotoImageView() {
        binding.photoImageView.setOnClickListener {
            if (selectedUri == null) {
                startPicker()
            }
        }
    }

    private fun startPicker() {
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

    private fun showProgress() {
        binding.progressBarLayout.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBarLayout.isVisible = false
    }

    private fun uploadImage(
        uri: Uri,
        successHandler: (String) -> Unit,
        errorHandler: (Throwable?) -> Unit
    ) {
        val fileName = "${UUID.randomUUID()}.png"
        Firebase.storage.reference.child("articles/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Firebase.storage.reference.child("articles/photo/$fileName").downloadUrl
                        .addOnSuccessListener {
                            successHandler(it.toString())
                        }.addOnFailureListener {
                            errorHandler(it)
                        }

                } else {
                    errorHandler(it.exception)
                }
            }
    }

    private fun uploadArticle(photoUrl: String, description: String) {
        val articleId = UUID.randomUUID().toString()    // 문서 이름
        val articleModel = ArticleModel(
            articleId = articleId,
            createdAt = System.currentTimeMillis(),
            description = description,
            imageUrl = photoUrl,
        )

        Firebase.firestore.collection("articles").document(articleId)
            .set(articleModel)
            .addOnSuccessListener {
                findNavController().navigate(WriteArticleFragmentDirections.actionWriteArticleFragmentToHomeFragment())
                hideProgress()
            }
            .addOnFailureListener {
                it.printStackTrace()
                view?.let { view ->
                    Snackbar.make(view, "글 작성에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                }
                hideProgress()
            }

        hideProgress()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            findNavController().navigate(WriteArticleFragmentDirections.actionBack())
        }
    }
}