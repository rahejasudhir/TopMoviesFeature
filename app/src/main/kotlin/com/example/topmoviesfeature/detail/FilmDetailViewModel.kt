package com.example.topmoviesfeature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.auth.AuthManager
import com.example.topmoviesfeature.auth.GuestSessionManager
import com.example.topmoviesfeature.detail.SimilarFilmMapper.toSimilarFilm
import com.example.topmoviesfeature.main.FilmRepository
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
    private val authManager: AuthManager,
    private val guestSessionManager: GuestSessionManager
) : ViewModel() {

    private val filmId: Long = savedStateHandle.get<Long>(FILM_ID)!!

    private val _filmDetailState = MutableStateFlow<FilmDetailState>(FilmDetailState.Loading)

    val filmDetailState = _filmDetailState.asStateFlow()

    private val _similarFilms = MutableStateFlow<List<SimilarFilm>>(emptyList())
    val similarFilm = _similarFilms.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    val sortType = _sortType.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authManager.isLoggedIn())
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _watchlistMessage = MutableStateFlow<String?>(null)
    val watchlistMessage = _watchlistMessage.asStateFlow()

    private val _currentRating = MutableStateFlow(0f)
    val currentRating = _currentRating.asStateFlow()

    private val _ratingMessage = MutableStateFlow<String?>(null)
    val ratingMessage = _ratingMessage.asStateFlow()

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
        if (!authManager.isLoggedIn()) {
            _watchlistMessage.value = "Please login to add to watchlist"
            return
        }

        viewModelScope.launch {
            try {
                val added = filmRepository.addToWatchlist(
                    authManager.getAccountId(),
                    authManager.getSessionId(),
                    filmId,
                    true
                )
                if (added) {
                    _watchlistMessage.value = "Added to watchlist"
                } else {
                    _watchlistMessage.value = "Failed to add to watchlist"
                }
            } catch (e: Exception) {
                _watchlistMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }

    fun rateMovie(filmId: Long, rating: Float) {
        viewModelScope.launch {
            try {
                var guestSessionId = guestSessionManager.getGuestSessionId()

                if (guestSessionId == null) {
                    guestSessionId = filmRepository.createGuestSession()
                    guestSessionManager.saveGuestSession(guestSessionId)
                }

                val success = filmRepository.rateMovie(guestSessionId, filmId, rating)

                if (success) {
                    _currentRating.value = rating
                    _ratingMessage.value = "Rated ${"★".repeat(rating.toInt())} ($rating/10)"
                } else {
                    _ratingMessage.value = "Failed to save rating"
                }
            } catch (e: Exception) {
                _ratingMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteRating(filmId: Long) {
        viewModelScope.launch {
            try {
                val guestSessionId = guestSessionManager.getGuestSessionId()

                if (guestSessionId != null) {
                    val success = filmRepository.deleteRating(guestSessionId, filmId)

                    if (success) {
                        _currentRating.value = 0f
                        _ratingMessage.value = "Rating removed"
                    }
                }
            } catch (e: Exception) {
                _ratingMessage.value = "Error: ${e.message}"
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
