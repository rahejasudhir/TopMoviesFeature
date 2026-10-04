package biz.filmeroo.premier.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.detail.SimilarFilmMapper.toSimilarFilm
import biz.filmeroo.premier.main.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    val filmDetailState = _filmDetailState.asStateFlow()

    private val _similarFilms = MutableStateFlow<List<SimilarFilm>>(emptyList())
    val similarFilm = _similarFilms.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val films = filmRepository.fetchMovie(filmId)
                _filmDetailState.update { FilmDetailState.Success(films) }
            } catch (e: Exception) {
                _filmDetailState.update { FilmDetailState.Error }
            }
        }

        viewModelScope.launch {
            try {
                val films = coroutineScope {
                    val similar = async { filmRepository.fetchSimilar(filmId) }
                    val genres = async { fetchGenresOrEmpty() }
                    similar.await().map { it.toSimilarFilm(genres.await()) }
                }
                _similarFilms.update { films }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _similarFilms.update { emptyList() }
            }
        }
    }

    private suspend fun fetchGenresOrEmpty(): Map<Int, String> {
        return try {
            filmRepository.fetchGenres()
        } catch (e: Exception) {
            emptyMap()
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
