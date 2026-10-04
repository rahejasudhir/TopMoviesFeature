package com.example.topmoviesfeature.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : androidx.appcompat.app.AppCompatActivity() {

    @Inject
    internal lateinit var adapter: FilmAdapter

    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()

        lifecycleScope.launch {
            viewModel.searchResults.collectLatest {
                showResults(it)
                binding.progressBar.visibility = android.view.View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                if (isLoading) {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    private fun setupView() {
        binding.cancelButton.setOnClickListener { finish() }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        binding.search.searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                viewModel.updateQuery(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

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
            viewModel.updateSort(sortBy)
            true
        }

        popupMenu.show()
    }

    private fun showResults(results: List<ApiFilm>) {
        adapter.submitList(results)
        binding.recycler.post {
            binding.recycler.scrollToPosition(0)
        }
    }
}
