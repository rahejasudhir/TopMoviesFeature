package com.example.topmoviesfeature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.detail.SimilarFilmMapper.toSimilarFilm
import com.example.topmoviesfeature.main.FilmRepository
import com.example.topmoviesfeature.watchlist.WatchlistManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FilmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val filmRepository: FilmRepository,
    private val watchlistManager: WatchlistManager
) : ViewModel() {

    private val filmId: Long = savedStateHandle.get<Long>(FILM_ID)!!

    private val _filmDetailState = MutableStateFlow<FilmDetailState>(FilmDetailState.Loading)

    val filmDetailState = _filmDetailState.asStateFlow()

    private val _similarFilms = MutableStateFlow<List<SimilarFilm>>(emptyList())
    val similarFilm = _similarFilms.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    val sortType = _sortType.asStateFlow()

    private val _watchlist = MutableStateFlow(watchlistManager.getWatchlist())
    val watchlist = _watchlist.asStateFlow()

    val sortedSimilarFilms = combine(_similarFilms, _sortType) { films, sort ->
        sort.sort(films)
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

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

    fun setSortType(sort: SortType) {
        _sortType.value = sort
    }

    fun toggleWatchlist(filmId: Long) {
        if (watchlistManager.isInWatchlist(filmId)) {
            watchlistManager.removeFromWatchlist(filmId)
        } else {
            watchlistManager.addToWatchlist(filmId)
        }
        _watchlist.update { watchlistManager.getWatchlist() }
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
