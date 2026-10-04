package com.example.topmoviesfeature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FilmViewModel @Inject constructor(
    private val filmRepository: FilmRepository
) : androidx.lifecycle.ViewModel() {

    private val _filmState = MutableStateFlow<FilmState>(FilmState.Loading)
    val filmState: Flow<FilmState> = _filmState

    private val _sortBy = MutableStateFlow(SortBy.NONE)
    private val _currentPage = MutableStateFlow(1)
    private val _allFilms = MutableStateFlow(emptyList<ApiFilm>())
    private val _isLoading = MutableStateFlow(false)
    private val _hasMore = MutableStateFlow(true)

    val films = combine(_allFilms, _sortBy) { films, sortBy ->
        sortResults(films, sortBy)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val hasMore = _hasMore.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    init {
        loadInitialFilms()
    }

    private fun loadInitialFilms() {
        viewModelScope.launch {
            try {
                _filmState.update { FilmState.Loading }
                val films = filmRepository.fetchTopRated(page = 1)
                _allFilms.value = films
                _currentPage.value = 1
                _hasMore.value = films.isNotEmpty()
                _filmState.update { FilmState.Success(films) }
            } catch (e: Exception) {
                _filmState.update { FilmState.Error }
            }
        }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val nextPage = _currentPage.value + 1
                val moreFilms = filmRepository.fetchTopRated(page = nextPage)

                _allFilms.value = _allFilms.value + moreFilms
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

    sealed interface FilmState {
        data object Loading : FilmState
        data class Success(val films: List<ApiFilm>) : FilmState
        data object Error : FilmState
    }
}
