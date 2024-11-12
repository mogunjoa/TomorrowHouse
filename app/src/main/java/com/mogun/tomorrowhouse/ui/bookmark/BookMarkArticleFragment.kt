package com.mogun.tomorrowhouse.ui.bookmark

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.mogun.tomorrowhouse.databinding.FragmentBookmarkBinding
import com.mogun.tomorrowhouse.R
import com.mogun.tomorrowhouse.data.ArticleModel

class BookMarkArticleFragment: Fragment(R.layout.fragment_bookmark) {
    private lateinit var binding: FragmentBookmarkBinding
    private lateinit var bookmarkAdapter: BookmarkArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookmarkBinding.bind(view)

        binding.toolbar.setupWithNavController(findNavController())

        bookmarkAdapter = BookmarkArticleAdapter {
            findNavController().navigate(
                BookMarkArticleFragmentDirections.actionBookMarkArticleFragmentToArticleFragment(
                    it.articleId.orEmpty()
                )
            )
        }

        binding.articleRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = bookmarkAdapter
        }

        val uid = Firebase.auth.currentUser?.uid.orEmpty()
        Firebase.firestore.collection("bookmark")
            .document(uid)
            .get()
            .addOnSuccessListener {
                val list = it.get("articleIds") as List<*>
                if(list.isNotEmpty()) {
                    Firebase.firestore.collection("articles").whereIn("articleId", list)
                        .get()
                        .addOnSuccessListener { result ->
                            bookmarkAdapter.submitList(result.map { article -> article.toObject<ArticleModel>() })
                        }.addOnFailureListener { e ->
                            e.printStackTrace()
                        }
                }
            }.addOnFailureListener {
                it.printStackTrace()
                Snackbar.make(view, "데이터를 가져오는 데 실패했습니다.", Snackbar.LENGTH_LONG).show()
            }
    }
}