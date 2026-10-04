package biz.filmeroo.premier.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.main.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FilmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    filmRepository: FilmRepository
) : ViewModel() {

    private val filmId: Long = savedStateHandle.get<Long>(FILM_ID)!!

    private val _filmDetailState = MutableStateFlow<FilmDetailState>(FilmDetailState.Loading)

    val filmDetailState = _filmDetailState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val films = filmRepository.fetchMovie(filmId)
                _filmDetailState.update { FilmDetailState.Success(films) }
            } catch (e: Exception) {
                _filmDetailState.update { FilmDetailState.Error }
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
