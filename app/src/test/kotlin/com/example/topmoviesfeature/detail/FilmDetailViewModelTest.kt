package biz.filmeroo.com.example.topmoviesfeature.detail

import androidx.lifecycle.SavedStateHandle
import biz.filmeroo.com.example.topmoviesfeature.CoroutineTestRule
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.detail.FilmDetailViewModel
import com.example.topmoviesfeature.detail.FilmDetailViewModel.Companion.FILM_ID
import com.example.topmoviesfeature.detail.FilmDetailViewModel.FilmDetailState
import com.example.topmoviesfeature.main.FilmRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FilmDetailViewModelTest {

    @get:Rule
    val rule: TestRule = CoroutineTestRule(StandardTestDispatcher())

    private val repository: FilmRepository = mock()
    private val savedStateHandle: SavedStateHandle = mock()

    private lateinit var viewModel: FilmDetailViewModel

    @Test
    fun `on init sets loading state`() = runTest {
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        viewModel = FilmDetailViewModel(savedStateHandle, repository)

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Loading)
    }

    @Test
    fun `on success shows film details`() = runTest {
        val film = mock<ApiFilm>()
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)

        viewModel = FilmDetailViewModel(savedStateHandle, repository)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Success(film))
    }

    @Test
    fun `on failure shows error`() = runTest {
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenAnswer { throw Exception("test_exception") }

        viewModel = FilmDetailViewModel(savedStateHandle, repository)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Error)
    }
}
