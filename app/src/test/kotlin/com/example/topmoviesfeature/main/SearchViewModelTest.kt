package biz.filmeroo.com.example.topmoviesfeature.main

import biz.filmeroo.com.example.topmoviesfeature.support.Fixtures
import biz.filmeroo.premier.main.FilmRepository
import biz.filmeroo.premier.main.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

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
}
