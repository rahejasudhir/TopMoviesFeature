package com.example.topmoviesfeature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.main.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FilmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val filmRepository: FilmRepository
) : ViewModel() {

    private val filmId: Long = savedStateHandle.get<Long>(FILM_ID)!!

    private val _filmDetailState = MutableStateFlow<FilmDetailState>(FilmDetailState.Loading)
    private val _similarFilms = MutableStateFlow(emptyList<ApiFilm>())

    val filmDetailState = _filmDetailState.asStateFlow()
    val similarFilms = _similarFilms.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val film = filmRepository.fetchMovie(filmId)
                _filmDetailState.update { FilmDetailState.Success(film) }
            } catch (e: Exception) {
                _filmDetailState.update { FilmDetailState.Error }
            }
        }

        viewModelScope.launch {
            try {
                val similar = filmRepository.similar(filmId, page = 1)
                _similarFilms.value = similar.take(6)
            } catch (e: Exception) {
                _similarFilms.value = emptyList()
            }
        }
    }

    internal companion object {
        internal const val FILM_ID = "filmId"
    }

    sealed interface FilmDetailState {
        data object Loading : FilmDetailState
        data class Success(val film: ApiFilm) : FilmDetailState
        data object Error : FilmDetailState
    }
}
