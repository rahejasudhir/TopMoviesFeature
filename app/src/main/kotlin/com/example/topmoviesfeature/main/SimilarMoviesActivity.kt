package com.example.topmoviesfeature.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.databinding.ActivitySimilarMoviesBinding
import com.example.topmoviesfeature.detail.FilmDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SimilarMoviesActivity : AppCompatActivity() {

    @Inject
    internal lateinit var adapter: FilmAdapter

    private val viewModel by viewModels<SimilarMoviesViewModel>()
    private lateinit var binding: ActivitySimilarMoviesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimilarMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupView()

        lifecycleScope.launch {
            viewModel.similarFilms.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { films ->
                displayResults(films)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { isLoading ->
                if (isLoading) {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupView() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        adapter.setOnClick { id -> FilmDetailActivity.start(this, id) }

        binding.sortFilter.setOnClickListener {
            showSortMenu(it)
        }

        binding.loadMoreButton.setOnClickListener {
            viewModel.loadMore()
        }

        lifecycleScope.launch {
            viewModel.hasMore.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { hasMore ->
                binding.loadMoreButton.visibility = if (hasMore) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showSortMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menu.add(0, 0, 0, "No Sort")
        popupMenu.menu.add(0, 1, 1, "A - Z")
        popupMenu.menu.add(0, 2, 2, "Z - A")
        popupMenu.menu.add(0, 3, 3, "Lowest to Highest Rating")
        popupMenu.menu.add(0, 4, 4, "Highest to Lowest Rating")

        popupMenu.setOnMenuItemClickListener { item ->
            val sortBy = when (item.itemId) {
                0 -> SortBy.NONE
                1 -> SortBy.TITLE_ASC
                2 -> SortBy.TITLE_DESC
                3 -> SortBy.RATING_ASC
                4 -> SortBy.RATING_DESC
                else -> SortBy.NONE
            }
            viewModel.updateSort(sortBy)
            true
        }

        popupMenu.show()
    }

    private fun displayResults(results: List<ApiFilm>) {
        adapter.submitList(results)
        binding.progressBar.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.recycler.scrollToPosition(0)
        }, 100)
    }

    companion object {
        const val MOVIE_ID = "movieId"

        fun start(origin: Activity, movieId: Long) {
            origin.startActivity(
                Intent(origin, SimilarMoviesActivity::class.java).apply {
                    putExtra(MOVIE_ID, movieId)
                }
            )
        }
    }
}
