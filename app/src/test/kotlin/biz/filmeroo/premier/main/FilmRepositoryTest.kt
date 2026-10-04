package biz.filmeroo.premier.main

import biz.filmeroo.premier.CoroutineTestRule
import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.api.ApiGenreListResponse
import biz.filmeroo.premier.api.FilmService
import biz.filmeroo.premier.support.Fixtures
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
    fun `fetches similar films and returns list`() = runTest {
        whenever(service.similar(2)).thenReturn(Fixtures.filmResponse())

        val films = repository.fetchSimilar(2)

        assertThat(films).isEqualTo(Fixtures.filmList())
    }

    @Test(expected = HttpException::class)
    fun `fetches similar films throws an exception`() = runTest {
        whenever(service.similar(2)).thenAnswer { throw HttpException(mock()) }

        repository.fetchSimilar(2)
    }

    @Test
    fun `fetches genres and returns map`() = runTest {
        whenever(service.genres()).thenReturn(Fixtures.genreResponse())

        val genres = repository.fetchGenres()

        verify(service).genres()
        assertThat(genres).containsEntry(1, "Action")
            .containsEntry(2, "Comedy")
            .containsEntry(3, "Drama")
    }

    @Test
    fun `fetches genres returns correct size map`() = runTest {
        whenever(service.genres()).thenReturn(Fixtures.genreResponse())

        val genres = repository.fetchGenres()

        assertThat(genres).hasSize(3)
    }

    @Test(expected = HttpException::class)
    fun `fetches genres throws an exception`() = runTest {
        whenever(service.genres()).thenAnswer { throw HttpException(mock()) }

        repository.fetchGenres()
    }

    @Test
    fun `fetches genres with single genre`() = runTest {
        whenever(service.genres()).thenReturn(
            ApiGenreListResponse(listOf(Fixtures.genre(1, "Action")))
        )

        val genres = repository.fetchGenres()

        assertThat(genres).hasSize(1)
            .containsEntry(1, "Action")
    }

    @Test
    fun `fetches genres with empty list`() = runTest {
        whenever(service.genres()).thenReturn(ApiGenreListResponse(emptyList()))

        val genres = repository.fetchGenres()

        assertThat(genres).isEmpty()
    }
}
