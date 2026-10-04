package com.example.topmoviesfeature.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SimilarMoviesViewModel @Inject constructor(
    private val filmRepository: FilmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Long = savedStateHandle["movieId"] ?: 0L

    private val _similarFilms = MutableStateFlow(emptyList<ApiFilm>())
    private val _sortBy = MutableStateFlow(SortBy.NONE)
    private val _currentPage = MutableStateFlow(1)
    private val _isLoading = MutableStateFlow(false)
    private val _hasMore = MutableStateFlow(true)

    val similarFilms = combine(_similarFilms, _sortBy) { films, sortBy ->
        sortResults(films, sortBy)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isLoading = _isLoading.asStateFlow()
    val hasMore = _hasMore.asStateFlow()

    init {
        loadInitialSimilarFilms()
    }

    private fun loadInitialSimilarFilms() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val films = filmRepository.similar(movieId, page = 1)
                _similarFilms.value = films
                _currentPage.value = 1
                _hasMore.value = films.isNotEmpty()
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val nextPage = _currentPage.value + 1
                val moreFilms = filmRepository.similar(movieId, page = nextPage)

                _similarFilms.value = _similarFilms.value + moreFilms
                _currentPage.value = nextPage
                _hasMore.value = moreFilms.isNotEmpty()
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun updateSort(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    private fun sortResults(films: List<ApiFilm>, sortBy: SortBy): List<ApiFilm> = when (sortBy) {
        SortBy.NONE -> films
        SortBy.TITLE_ASC -> films.sortedBy { it.title }
        SortBy.TITLE_DESC -> films.sortedByDescending { it.title }
        SortBy.RATING_ASC -> films.sortedBy { it.voteAverage?.toFloatOrNull() ?: 0f }
        SortBy.RATING_DESC -> films.sortedByDescending { it.voteAverage?.toFloatOrNull() ?: 0f }
    }
}
