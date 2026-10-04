package com.example.topmoviesfeature.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.topmoviesfeature.R
import com.example.topmoviesfeature.api.FilmService
import com.example.topmoviesfeature.databinding.ItemSimilarFilmBinding
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import javax.inject.Inject

class SimilarFilmAdapter @Inject constructor(private val picasso: Picasso) :
    ListAdapter<SimilarFilm, SimilarFilmAdapter.Holder>(diffCallback) {

    private var onClick: ((Long) -> Unit)? = null
    private var onWatchlistToggle: ((Long) -> Unit)? = null
    private var watchlistedIds: Set<Long> = emptySet()

    fun setOnClick(onClick: (Long) -> Unit) {
        this.onClick = onClick
    }

    fun setOnWatchlistToggle(onToggle: (Long) -> Unit) {
        this.onWatchlistToggle = onToggle
    }

    fun setWatchlist(ids: Set<Long>) {
        watchlistedIds = ids
        notifyDataSetChanged()
    }

    public override fun getItem(position: Int): SimilarFilm {
        return super.getItem(position)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Holder {
        val binding = ItemSimilarFilmBinding.inflate(LayoutInflater.from(p0.context), p0, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemSimilarFilmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SimilarFilm) {
            binding.title.text = item.title
            binding.genres.text = item.genres
            binding.rating.text = item.rating
            binding.rating.visibility = if (item.rating.isEmpty()) View.GONE else View.VISIBLE
            val cornerRadius =
                binding.root.resources.getDimensionPixelSize(R.dimen.image_corner_radius)
            picasso
                .load(item.posterPath?.let(FilmService::buildImageUrl))
                .transform(RoundedCornersTransformation(cornerRadius, 0))
                .into(binding.image)

            val isInWatchlist = watchlistedIds.contains(item.id)
            updateWatchlistButton(isInWatchlist)

            binding.watchlistButton.setOnClickListener {
                onWatchlistToggle?.invoke(item.id)
            }

            binding.root.setOnClickListener { onClick?.invoke(item.id) }
        }

        private fun updateWatchlistButton(isInWatchlist: Boolean) {
            binding.watchlistButton.setImageResource(
                if (isInWatchlist) {
                    android.R.drawable.ic_dialog_info
                } else {
                    android.R.drawable.ic_input_get
                }
            )
        }
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<SimilarFilm>() {
            override fun areContentsTheSame(old: SimilarFilm, new: SimilarFilm): Boolean {
                return old == new
            }

            override fun areItemsTheSame(old: SimilarFilm, new: SimilarFilm): Boolean {
                return old.id == new.id
            }
        }
    }
}
