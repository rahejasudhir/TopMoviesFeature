package biz.filmeroo.premier.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import biz.filmeroo.premier.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FilmViewModel @Inject constructor(
    filmRepository: FilmRepository
) : androidx.lifecycle.ViewModel() {

    private val _filmState = MutableStateFlow<FilmState>(FilmState.Loading)
    val filmState: Flow<FilmState> = _filmState

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

    sealed interface FilmState {
        data object Loading : FilmState
        data class Success(val films: List<ApiFilm>) : FilmState
        data object Error : FilmState
    }
}
