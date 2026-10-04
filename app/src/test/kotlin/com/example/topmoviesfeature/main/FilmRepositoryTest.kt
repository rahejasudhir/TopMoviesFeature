package biz.filmeroo.com.example.topmoviesfeature.main

import biz.filmeroo.com.example.topmoviesfeature.CoroutineTestRule
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.api.FilmService
import biz.filmeroo.com.example.topmoviesfeature.support.Fixtures
import com.example.topmoviesfeature.main.FilmRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import retrofit2.HttpException

class FilmRepositoryTest {

    @get:Rule
    val rule: TestRule = CoroutineTestRule()

    private val service: FilmService = mock()

    private lateinit var repository: FilmRepository

    @Before
    fun setUp() {
        repository = FilmRepository(service)
    }

    @Test
    fun `fetches top rated films and returns list`() = runTest {
        whenever(service.topRated()).thenReturn(Fixtures.filmResponse())

        val films = repository.fetchTopRated()

        assertThat(films).isEqualTo(Fixtures.filmList())
    }

    @Test(expected = HttpException::class)
    fun `fetches top rated films throws an exception`() = runTest {
        whenever(service.topRated()).thenAnswer { throw HttpException(mock()) }

        repository.fetchTopRated()
    }

    @Test
    fun `fetches movie details`() = runTest {
        val movie = mock<ApiFilm>()
        whenever(service.movie(2)).thenReturn(movie)

        val result = repository.fetchMovie(2)

        verify(service).movie(2)
        assertThat(result).isEqualTo(movie)
    }

    @Test(expected = HttpException::class)
    fun `fetches movie details throws an exception`() = runTest {
        whenever(service.movie(2)).thenAnswer { throw HttpException(mock()) }

        repository.fetchMovie(2)
    }

    @Test
    fun `searches films and returns results list single`(): Unit = runBlocking {
        val query = "shawshank redemption"
        whenever(service.search(query)).thenReturn(Fixtures.filmResponse())

        repository.search(query)

        verify(service).search(query)
    }
}
