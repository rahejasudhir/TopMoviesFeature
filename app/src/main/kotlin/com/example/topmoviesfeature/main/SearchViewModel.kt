package com.example.topmoviesfeature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SearchViewModel @Inject constructor(
    private val filmRepository: FilmRepository
) : androidx.lifecycle.ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow(emptyList<ApiFilm>())

    private val _sortBy = MutableStateFlow(SortBy.NONE)

    val searchResults = combine(_searchResults, _sortBy) { results, sortBy ->
        sortResults(results, sortBy)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    fun updateSort(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    private fun sortResults(results: List<ApiFilm>, sortBy: SortBy): List<ApiFilm> = when (sortBy) {
        SortBy.NONE -> results
        SortBy.TITLE_ASC -> results.sortedBy { it.title }
        SortBy.TITLE_DESC -> results.sortedByDescending { it.title }
        SortBy.RATING_ASC -> results.sortedBy { it.voteAverage?.toFloatOrNull() ?: 0f }
        SortBy.RATING_DESC -> results.sortedByDescending { it.voteAverage?.toFloatOrNull() ?: 0f }
    }
}
