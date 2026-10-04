package biz.filmeroo.com.example.topmoviesfeature.main

import biz.filmeroo.com.example.topmoviesfeature.support.Fixtures
import com.example.topmoviesfeature.main.FilmRepository
import com.example.topmoviesfeature.main.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)

class SearchViewModelTest {

    private val repository: FilmRepository = mock()

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        viewModel = SearchViewModel(repository)
    }

    @Test
    fun `updating query fetches and displays search results`(): Unit = runTest {
        val results = Fixtures.filmList()
        whenever(repository.search("shawshank redemption")).thenReturn(results)

        viewModel.updateQuery("shawshank redemption")
        advanceUntilIdle()

        assertThat(viewModel.searchResults.first()).isEqualTo(results)
    }

    @Test
    fun `rapid queries emit only final result after debounce`(): Unit = runTest {
        val results = Fixtures.filmList()
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("m")
        advanceTimeBy(50)
        viewModel.updateQuery("ma")
        advanceTimeBy(50)
        viewModel.updateQuery("mov")
        advanceTimeBy(50)
        viewModel.updateQuery("movie")
        advanceTimeBy(300)

        advanceUntilIdle()

        verify(repository, times(1)).search("movie")
        assertThat(viewModel.searchResults.first()).isEqualTo(results)
    }

    @Test
    fun `duplicate queries do not trigger multiple searches`(): Unit = runTest {
        val results = Fixtures.filmList()
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        verify(repository, times(1)).search("movie")
    }

    @Test
    fun `empty query does not trigger search`(): Unit = runTest {
        viewModel.updateQuery("")
        advanceTimeBy(300)
        advanceUntilIdle()

        verify(repository, never()).search("")
        assertThat(viewModel.searchResults.first()).isEmpty()
    }

    @Test
    fun `loading state is true during search and false after`(): Unit = runTest {
        val results = Fixtures.filmList()
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertThat(viewModel.isLoading.first()).isFalse
    }

    @Test
    fun `search returns correct results from repository`(): Unit = runTest {
        val expectedFilms = Fixtures.filmList()
        whenever(repository.search("movie")).thenReturn(expectedFilms)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        assertThat(viewModel.searchResults.first()).isEqualTo(expectedFilms)
    }

    @Test
    fun `new query cancels previous search`(): Unit = runTest {
        val result1 = listOf(Fixtures.film(1))
        val result2 = Fixtures.filmList()

        whenever(repository.search("m")).thenReturn(result1)
        whenever(repository.search("movie")).thenReturn(result2)

        viewModel.updateQuery("m")
        advanceTimeBy(300)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        verify(repository).search("m")
        verify(repository).search("movie")
        assertThat(viewModel.searchResults.first()).isEqualTo(result2)
    }
}
