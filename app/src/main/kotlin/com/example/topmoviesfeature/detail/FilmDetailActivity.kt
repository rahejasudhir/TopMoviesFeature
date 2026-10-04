package biz.filmeroo.premier.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import biz.filmeroo.premier.R
import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.api.FilmService
import biz.filmeroo.premier.databinding.ActivityDetailBinding
import biz.filmeroo.premier.detail.FilmDetailViewModel.FilmDetailState
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FilmDetailActivity : AppCompatActivity() {

    @Inject
    internal lateinit var picasso: Picasso

    private lateinit var binding: ActivityDetailBinding

    private val filmDetailViewModel by viewModels<FilmDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        lifecycleScope.launch {
            filmDetailViewModel.filmDetailState.flowWithLifecycle(lifecycle).collect(::updateState)
        }
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
