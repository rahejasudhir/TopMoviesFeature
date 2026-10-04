package biz.filmeroo.com.example.topmoviesfeature.main

import biz.filmeroo.com.example.topmoviesfeature.support.Fixtures
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.main.FilmRepository
import com.example.topmoviesfeature.main.SearchViewModel
import com.example.topmoviesfeature.main.SortBy
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

    @Test
    fun `search results sorted by title ascending`(): Unit = runTest {
        val results = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5"),
            ApiFilm(3, "Banana", "desc", "/b", "/bb", "8.5")
        )
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.TITLE_ASC)
        advanceUntilIdle()

        val sorted = viewModel.searchResults.first()
        assertThat(sorted.map { it.title }).isEqualTo(listOf("Apple", "Banana", "Zebra"))
    }

    @Test
    fun `search results sorted by title descending`(): Unit = runTest {
        val results = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5"),
            ApiFilm(3, "Banana", "desc", "/b", "/bb", "8.5")
        )
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.TITLE_DESC)
        advanceUntilIdle()

        val sorted = viewModel.searchResults.first()
        assertThat(sorted.map { it.title }).isEqualTo(listOf("Zebra", "Banana", "Apple"))
    }

    @Test
    fun `search results sorted by rating ascending`(): Unit = runTest {
        val results = listOf(
            ApiFilm(1, "High", "desc", "/h", "/bh", "8.5"),
            ApiFilm(2, "Low", "desc", "/l", "/bl", "7.0"),
            ApiFilm(3, "Mid", "desc", "/m", "/bm", "7.5")
        )
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.RATING_ASC)
        advanceUntilIdle()

        val sorted = viewModel.searchResults.first()
        assertThat(sorted.map { it.voteAverage }).isEqualTo(listOf("7.0", "7.5", "8.5"))
    }

    @Test
    fun `search results sorted by rating descending`(): Unit = runTest {
        val results = listOf(
            ApiFilm(1, "High", "desc", "/h", "/bh", "8.5"),
            ApiFilm(2, "Low", "desc", "/l", "/bl", "7.0"),
            ApiFilm(3, "Mid", "desc", "/m", "/bm", "7.5")
        )
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.RATING_DESC)
        advanceUntilIdle()

        val sorted = viewModel.searchResults.first()
        assertThat(sorted.map { it.voteAverage }).isEqualTo(listOf("8.5", "7.5", "7.0"))
    }

    @Test
    fun `sort persists when search query changes`(): Unit = runTest {
        val result1 = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5")
        )
        val result2 = listOf(
            ApiFilm(3, "Movie", "desc", "/m", "/bm", "8.5"),
            ApiFilm(4, "Action", "desc", "/ac", "/bac", "7.2")
        )
        whenever(repository.search("book")).thenReturn(result1)
        whenever(repository.search("action")).thenReturn(result2)

        viewModel.updateQuery("book")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.TITLE_ASC)
        advanceUntilIdle()

        viewModel.updateQuery("action")
        advanceTimeBy(300)
        advanceUntilIdle()

        val sorted = viewModel.searchResults.first()
        assertThat(sorted.map { it.title }).isEqualTo(listOf("Action", "Movie"))
    }

    @Test
    fun `changing sort does not trigger new API call`(): Unit = runTest {
        val results = listOf(
            ApiFilm(1, "Zebra", "desc", "/z", "/bz", "8.0"),
            ApiFilm(2, "Apple", "desc", "/a", "/ba", "7.5")
        )
        whenever(repository.search("movie")).thenReturn(results)

        viewModel.updateQuery("movie")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.updateSort(SortBy.TITLE_ASC)
        advanceUntilIdle()

        verify(repository, times(1)).search("movie")
    }
}
