package biz.filmeroo.premier.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import biz.filmeroo.premier.api.ApiFilm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val filmRepository: FilmRepository
) : androidx.lifecycle.ViewModel() {

    val searchResults = MutableStateFlow(emptyList<ApiFilm>())

    fun updateQuery(query: String) {
        viewModelScope.launch {
            val result = filmRepository.search(query)
            searchResults.emit(result)
        }
    }
}
