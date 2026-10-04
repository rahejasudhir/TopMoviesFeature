package com.example.topmoviesfeature.detail

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.topmoviesfeature.R
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.api.FilmService
import com.example.topmoviesfeature.databinding.ActivityDetailBinding
import com.example.topmoviesfeature.detail.FilmDetailViewModel.FilmDetailState
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FilmDetailActivity : AppCompatActivity() {

    @Inject
    internal lateinit var picasso: Picasso

    @Inject
    internal lateinit var similarFilmAdapter: SimilarFilmAdapter

    private lateinit var binding: ActivityDetailBinding

    private val filmDetailViewModel by viewModels<FilmDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.similarRecyclerview.adapter = similarFilmAdapter
        similarFilmAdapter.setOnClick { id -> start(this, id) }
        similarFilmAdapter.setOnWatchlistToggle { id -> filmDetailViewModel.toggleWatchlist(id) }

        lifecycleScope.launch {
            filmDetailViewModel.filmDetailState.flowWithLifecycle(lifecycle).collect(::updateState)
        }

        lifecycleScope.launch {
            filmDetailViewModel.sortedSimilarFilms.flowWithLifecycle(lifecycle)
                .collect(::displaySimilarFilms)
        }

        lifecycleScope.launch {
            filmDetailViewModel.watchlist.flowWithLifecycle(lifecycle)
                .collect { watchlist -> similarFilmAdapter.setWatchlist(watchlist) }
        }

        setupSortMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_film_detail, menu)
        return true
    }

    private fun setupSortMenu() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sort -> {
                    showSortDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showSortDialog() {
        val items = arrayOf("Default Order", "Rating (High to Low)", "Title (A-Z)")
        AlertDialog.Builder(this)
            .setTitle("Sort by")
            .setItems(items) { _, which ->
                val sortType = when (which) {
                    0 -> SortType.DEFAULT
                    1 -> SortType.RATING_HIGH_TO_LOW
                    2 -> SortType.TITLE_A_TO_Z
                    else -> SortType.DEFAULT
                }
                filmDetailViewModel.setSortType(sortType)
            }
            .show()
    }

    private fun updateState(filmDetailState: FilmDetailState) {
        when (filmDetailState) {
            is FilmDetailState.Loading -> binding.progress.visibility = View.VISIBLE
            is FilmDetailState.Success -> displayMovie(filmDetailState.film)
            FilmDetailState.Error -> displayError()
        }
    }

    private fun displayMovie(movie: ApiFilm) {
        if (movie.backdropPath != null) {
            picasso.load(FilmService.buildImageUrl(movie.backdropPath)).into(binding.filmImage)
        }
        binding.filmTitle.text = movie.title
        binding.filmOverview.text = movie.overview

        binding.progress.visibility = View.GONE
    }

    private fun displaySimilarFilms(films: List<SimilarFilm>) {
        similarFilmAdapter.submitList(films)
        binding.similarGroup.visibility = if (films.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun displayError() {
        Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show()
        binding.progress.visibility = View.GONE
    }

    companion object {
        fun start(origin: Activity, id: Long) {
            origin.startActivity(
                Intent(origin, FilmDetailActivity::class.java).apply {
                    putExtra(FilmDetailViewModel.FILM_ID, id)
                }
            )
        }
    }
}
