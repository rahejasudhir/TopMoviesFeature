package com.example.topmoviesfeature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SearchViewModel @Inject constructor(
    private val filmRepository: FilmRepository
) : androidx.lifecycle.ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<ApiFilm>?>(null)
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        search(query)
                    }
                }
                .catch { _searchResults.value = emptyList() }
                .collect { results -> _searchResults.value = results }
        }
    }

    private fun search(query: String) = flow {
        _isLoading.value = true
        try {
            val results = filmRepository.search(query)
            emit(results)
            _isLoading.value = false
        } catch (e: Exception) {
            _isLoading.value = false
            throw e
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }
}
