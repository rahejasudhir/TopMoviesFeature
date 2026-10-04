package com.example.topmoviesfeature.main

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.topmoviesfeature.R
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.databinding.ActivityMainBinding
import com.example.topmoviesfeature.detail.FilmDetailActivity
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
        lifecycleScope.launch {
            filmViewModel.films.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect { films ->
                displayResults(films)
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
            try {
                startActivity(Intent(this, SearchActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening search: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.search.filter.setOnClickListener {
            showSortMenu(it)
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
            filmViewModel.updateSort(sortBy)
            true
        }

        popupMenu.show()
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
    }

    private fun displayError() {
        Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show()
        binding.progress.visibility = View.GONE
    }
}
