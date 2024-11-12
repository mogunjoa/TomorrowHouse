package com.mogun.tomorrowhouse.ui.article

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewModel: WriteArticleViewModel

    val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.updateSelectedUri(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWriteBinding.bind(view)

        setupViewModel()

        if(viewModel.selectedUri.value == null) {
            startPicker()
        }

        setupPhotoImageView()
        setupDelete()
        setupSubmitButton(view)
        setupBackButton()
        backPressed()
    }

    private fun backPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(binding.progressBarLayout.isVisible == false) {
                    findNavController().navigate(WriteArticleFragmentDirections.actionBack())
                }
            }
        })
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity())[WriteArticleViewModel::class.java]
        viewModel.selectedUri.observe(viewLifecycleOwner) {
            binding.photoImageView.setImageURI(it)
            binding.plusButton.isVisible = false
            binding.deleteButton.isVisible = true

            if (it != null) {
                binding.deleteButton.isVisible = true
                binding.plusButton.isVisible = false
            } else {
                binding.deleteButton.isVisible = false
                binding.plusButton.isVisible = true
            }
        }
    }

    private fun setupSubmitButton(view: View) {
        binding.submitButton.setOnClickListener {
            showProgress()
            if (viewModel.selectedUri.value != null) {
                val photoUri = viewModel.selectedUri.value ?: return@setOnClickListener
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
            viewModel.updateSelectedUri(null)
        }
    }

    private fun setupPhotoImageView() {
        binding.photoImageView.setOnClickListener {
            if (viewModel.selectedUri.value == null) {
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