package biz.filmeroo.com.example.topmoviesfeature.main

import biz.filmeroo.com.example.topmoviesfeature.CoroutineTestRule
import biz.filmeroo.com.example.topmoviesfeature.support.Fixtures
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.main.FilmRepository
import com.example.topmoviesfeature.main.FilmViewModel
import com.example.topmoviesfeature.main.FilmViewModel.FilmState
import com.example.topmoviesfeature.main.SortBy
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
class FilmViewModelTest {

    @get:Rule
    val rule: TestRule = CoroutineTestRule(StandardTestDispatcher())

    private val repository: FilmRepository = mock()

    private lateinit var viewModel: FilmViewModel

    @Test
    fun `on init sets loading state`() = runTest {
        viewModel = FilmViewModel(repository)

        assertThat(viewModel.filmState.first()).isEqualTo(FilmState.Loading)
    }

    @Test
    fun `on success show success state with films`() = runTest {
        val results = Fixtures.filmList()
        whenever(repository.fetchTopRated()).thenReturn(results)

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        assertThat(viewModel.filmState.first()).isEqualTo(FilmState.Success(results))
    }

    @Test
    fun `on failure shows error`() = runTest {
        whenever(repository.fetchTopRated()).thenAnswer { throw Exception("test_exception") }

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        assertThat(viewModel.filmState.first()).isEqualTo(FilmState.Error)
    }

    @Test
    fun `films are sorted by title ascending when sort changes`() = runTest {
        val results = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5"),
            ApiFilm(3, "Banana", "desc", "/b", "/bb", "8.5")
        )
        whenever(repository.fetchTopRated()).thenReturn(results)

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.TITLE_ASC)
        advanceUntilIdle()

        val sorted = viewModel.films.first()
        assertThat(sorted.map { it.title }).isEqualTo(listOf("Apple", "Banana", "Zebra"))
    }

    @Test
    fun `films are sorted by title descending when sort changes`() = runTest {
        val results = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5"),
            ApiFilm(3, "Banana", "desc", "/b", "/bb", "8.5")
        )
        whenever(repository.fetchTopRated()).thenReturn(results)

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.TITLE_DESC)
        advanceUntilIdle()

        val sorted = viewModel.films.first()
        assertThat(sorted.map { it.title }).isEqualTo(listOf("Zebra", "Banana", "Apple"))
    }

    @Test
    fun `films are sorted by rating ascending when sort changes`() = runTest {
        val results = listOf(
            ApiFilm(1, "High", "desc", "/h", "/bh", "8.5"),
            ApiFilm(2, "Low", "desc", "/l", "/bl", "7.0"),
            ApiFilm(3, "Mid", "desc", "/m", "/bm", "7.5")
        )
        whenever(repository.fetchTopRated()).thenReturn(results)

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.RATING_ASC)
        advanceUntilIdle()

        val sorted = viewModel.films.first()
        assertThat(sorted.map { it.voteAverage }).isEqualTo(listOf("7.0", "7.5", "8.5"))
    }

    @Test
    fun `films are sorted by rating descending when sort changes`() = runTest {
        val results = listOf(
            ApiFilm(1, "High", "desc", "/h", "/bh", "8.5"),
            ApiFilm(2, "Low", "desc", "/l", "/bl", "7.0"),
            ApiFilm(3, "Mid", "desc", "/m", "/bm", "7.5")
        )
        whenever(repository.fetchTopRated()).thenReturn(results)

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.RATING_DESC)
        advanceUntilIdle()

        val sorted = viewModel.films.first()
        assertThat(sorted.map { it.voteAverage }).isEqualTo(listOf("8.5", "7.5", "7.0"))
    }

    @Test
    fun `films unsorted when sort is NONE`() = runTest {
        val results = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5"),
            ApiFilm(3, "Banana", "desc", "/b", "/bb", "8.5")
        )
        whenever(repository.fetchTopRated()).thenReturn(results)

        viewModel = FilmViewModel(repository)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.NONE)
        advanceUntilIdle()

        val films = viewModel.films.first()
        assertThat(films.map { it.title }).isEqualTo(listOf("Zebra", "Apple", "Banana"))
    }
}
