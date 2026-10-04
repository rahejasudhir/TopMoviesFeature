package biz.filmeroo.premier.main

import biz.filmeroo.premier.CoroutineTestRule
import biz.filmeroo.premier.main.FilmViewModel.FilmState
import biz.filmeroo.premier.support.Fixtures
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
}
