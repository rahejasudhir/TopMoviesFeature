package com.example.topmoviesfeature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FilmViewModel @Inject constructor(
    filmRepository: FilmRepository
) : androidx.lifecycle.ViewModel() {

    private val _filmState = MutableStateFlow<FilmState>(FilmState.Loading)
    val filmState: Flow<FilmState> = _filmState

    private val _sortBy = MutableStateFlow(SortBy.NONE)

    val films = combine(_filmState, _sortBy) { state, sortBy ->
        when (state) {
            is FilmState.Success -> sortResults(state.films, sortBy)
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            try {
                val films = filmRepository.fetchTopRated()
                _filmState.update { FilmState.Success(films) }
            } catch (e: Exception) {
                _filmState.update { FilmState.Error }
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
