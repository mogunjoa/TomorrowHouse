package com.mogun.tomorrowhouse.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mogun.tomorrowhouse.data.ArticleModel
import com.mogun.tomorrowhouse.databinding.ItemArticleBinding

class HomeArticleAdapter(val onItemClicked: (ArticleModel) -> Unit): ListAdapter<ArticleModel, HomeArticleAdapter.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ArticleModel>() {
            override fun areItemsTheSame(
                oldItem: ArticleModel,
                newItem: ArticleModel
            ): Boolean {
                return oldItem.articleId == newItem.articleId
            }

            override fun areContentsTheSame(
                oldItem: ArticleModel,
                newItem: ArticleModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ItemArticleBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(articleModel: ArticleModel) {
            binding.descriptionTextView.text = articleModel.description

            Glide.with(binding.thumbnailImageView)
                .load(articleModel.imageUrl)
                .into(binding.thumbnailImageView)

            binding.root.setOnClickListener {
                onItemClicked(articleModel)
            }
        }
    }
}