package biz.filmeroo.premier.main

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import biz.filmeroo.premier.R
import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.databinding.ActivityMainBinding
import biz.filmeroo.premier.detail.FilmDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.appcompat.app.AppCompatActivity() {

    @Inject
    internal lateinit var adapter: FilmAdapter

    private lateinit var binding: ActivityMainBinding

    private val filmViewModel by viewModels<FilmViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setupView()
        lifecycleScope.launch {
            filmViewModel.filmState.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {
                updateState(it)
            }
        }
    }

    private fun setupView() {
        setContentView(binding.root)
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recycler.layoutManager = linearLayoutManager
        binding.recycler.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(this, linearLayoutManager.orientation).apply {
            setDrawable(AppCompatResources.getDrawable(this@MainActivity, R.drawable.divider)!!)
        }
        binding.recycler.addItemDecoration(dividerItemDecoration)
        adapter.setOnClick { id -> FilmDetailActivity.start(this, id) }
        binding.search.searchField.inputType = InputType.TYPE_NULL

        binding.search.searchField.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun updateState(filmState: FilmViewModel.FilmState) {
        when (filmState) {
            is FilmViewModel.FilmState.Loading -> {
                binding.progress.visibility = View.VISIBLE
            }
            is FilmViewModel.FilmState.Success -> displayResults(filmState.films)
            FilmViewModel.FilmState.Error -> displayError()
        }
    }

    private fun displayResults(results: List<ApiFilm>) {
        adapter.submitList(results)
        binding.progress.visibility = View.GONE
    }

    private fun displayError() {
        Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show()
        binding.progress.visibility = View.GONE
    }
}
