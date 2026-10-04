package com.example.topmoviesfeature.detail

import androidx.lifecycle.SavedStateHandle
import com.example.topmoviesfeature.CoroutineTestRule
import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.detail.FilmDetailViewModel.Companion.FILM_ID
import com.example.topmoviesfeature.auth.AuthManager
import com.example.topmoviesfeature.auth.GuestSessionManager
import com.example.topmoviesfeature.detail.FilmDetailViewModel.FilmDetailState
import com.example.topmoviesfeature.main.FilmRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
    private val authManager: AuthManager = mock()
    private val guestSessionManager: GuestSessionManager = mock()

    private lateinit var viewModel: FilmDetailViewModel

    @Test
    fun `on init sets loading state`() = runTest {
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Loading)
    }

    @Test
    fun `on success shows film details`() = runTest {
        val film = mock<ApiFilm>()
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Success(film))
    }

    @Test
    fun `on failure shows error`() = runTest {
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenAnswer { throw Exception("test_exception") }

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Error)
    }

    @Test
    fun `similar films are loaded with genre names`() = runTest {
        val filmId = 2L
        val film = mock<ApiFilm>()
        val similar = mock<ApiFilm>().apply {
            whenever(id).thenReturn(1)
            whenever(title).thenReturn("Title")
            whenever(posterPath).thenReturn("posterPath")
            whenever(voteAverage).thenReturn("1.12")
            whenever(genreIds).thenReturn(listOf(1, 2))
        }
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(listOf(similar))
        whenever(repository.fetchGenres()).thenReturn(mapOf(1 to "Genre1", 2 to "Genre2"))
        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()
        assertThat(viewModel.similarFilm.first()).containsExactly(
            SimilarFilm(
                1,
                "Title",
                "posterPath",
                "Genre1 * Genre2",
                "1.1"
            )
        )
    }

    @Test
    fun `failed to load similar films does not affect moviw details`() = runTest {
        val filmId = 1L
        val film = mock<ApiFilm>()
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenThrow(RuntimeException("Network exception"))
        whenever(repository.fetchGenres()).thenReturn(emptyMap())
        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()
        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Success(film))
        assertThat(viewModel.similarFilm.first()).isEmpty()
    }

    @Test
    fun `failing to load genres still shows similar filmns without genre names`() = runTest {
        val filmId = 2L
        val film = mock<ApiFilm>()
        val similar = mock<ApiFilm>().apply {
            whenever(id).thenReturn(1)
            whenever(title).thenReturn("Title")
            whenever(posterPath).thenReturn("posterPath")
            whenever(voteAverage).thenReturn("1.1")
            whenever(genreIds).thenReturn(listOf(1))
        }
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(listOf(similar))
        whenever(repository.fetchGenres()).thenThrow(RuntimeException("Network exception"))
        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val similarFilms = viewModel.similarFilm.first()
        assertThat(similarFilms).hasSize(1)
        assertThat(similarFilms[0].genres).isEmpty()
        assertThat(similarFilms[0].title).isEqualTo("Title")
    }

    @Test
    fun `failing to load genres and similar still shows movie details`() = runTest {
        val filmId = 2L
        val film = mock<ApiFilm>()
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenThrow(RuntimeException("Network exception"))
        whenever(repository.fetchGenres()).thenThrow(RuntimeException("Network exception"))
        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Success(film))
        assertThat(viewModel.similarFilm.first()).isEmpty()
    }

    @Test
    fun `both film and similar films fail simultaneously`() = runTest {
        val filmId = 1L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenThrow(RuntimeException("Film fetch failed"))
        whenever(repository.fetchSimilar(filmId)).thenThrow(RuntimeException("Similar fetch failed"))
        whenever(repository.fetchGenres()).thenThrow(RuntimeException("Genres fetch failed"))

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Error)
        assertThat(viewModel.similarFilm.first()).isEmpty()
    }

    @Test
    fun `multiple subscriptions to film state flow return same value`() = runTest {
        val film = mock<ApiFilm>()
        val filmId = 2L
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val firstSubscription = viewModel.filmDetailState.first()
        val secondSubscription = viewModel.filmDetailState.first()

        assertThat(firstSubscription).isEqualTo(secondSubscription)
        assertThat(firstSubscription).isEqualTo(FilmDetailState.Success(film))
    }

    @Test
    fun `multiple subscriptions to similar films flow return same value`() = runTest {
        val filmId = 2L
        val film = mock<ApiFilm>()
        val similar = mock<ApiFilm>().apply {
            whenever(id).thenReturn(1)
            whenever(title).thenReturn("Title")
            whenever(posterPath).thenReturn("posterPath")
            whenever(voteAverage).thenReturn("8.5")
            whenever(genreIds).thenReturn(listOf(1, 2))
        }
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(listOf(similar))
        whenever(repository.fetchGenres()).thenReturn(mapOf(1 to "Genre1", 2 to "Genre2"))

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val firstSubscription = viewModel.similarFilm.first()
        val secondSubscription = viewModel.similarFilm.first()

        assertThat(firstSubscription).isEqualTo(secondSubscription)
        assertThat(firstSubscription).hasSize(1)
    }

    @Test
    fun `handles delayed genres response gracefully`() = runTest {
        val filmId = 2L
        val film = mock<ApiFilm>()
        val similar = mock<ApiFilm>().apply {
            whenever(id).thenReturn(1)
            whenever(title).thenReturn("Title")
            whenever(posterPath).thenReturn("posterPath")
            whenever(voteAverage).thenReturn("8.5")
            whenever(genreIds).thenReturn(listOf(1, 2))
        }
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(listOf(similar))
        whenever(repository.fetchGenres()).thenAnswer { invocation ->
            advanceUntilIdle()
            mapOf(1 to "Genre1", 2 to "Genre2")
        }

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val similarFilms = viewModel.similarFilm.first()
        assertThat(similarFilms).hasSize(1)
        assertThat(similarFilms[0].genres).isEqualTo("Genre1 * Genre2")
    }

    @Test
    fun `handles large number of similar films efficiently`() = runTest {
        val filmId = 1L
        val film = mock<ApiFilm>()
        val largeFilmList = (1..150).map { id ->
            mock<ApiFilm>().apply {
                whenever(this.id).thenReturn(id.toLong())
                whenever(this.title).thenReturn("Film $id")
                whenever(this.posterPath).thenReturn("path$id.jpg")
                whenever(this.voteAverage).thenReturn("${(id % 10) + 5}.${id % 10}")
                whenever(this.genreIds).thenReturn(listOf(1, 2, 3))
            }
        }

        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(largeFilmList)
        whenever(repository.fetchGenres()).thenReturn(
            mapOf(1 to "Action", 2 to "Comedy", 3 to "Drama")
        )

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val similarFilms = viewModel.similarFilm.first()
        assertThat(similarFilms).hasSize(150)
        assertThat(similarFilms[0].title).isEqualTo("Film 1")
        assertThat(similarFilms[149].title).isEqualTo("Film 150")
    }

    @Test
    fun `handles very small film ids correctly`() = runTest {
        val filmId = 0L
        val film = mock<ApiFilm>()
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(emptyList())
        whenever(repository.fetchGenres()).thenReturn(emptyMap())

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Success(film))
        assertThat(viewModel.similarFilm.first()).isEmpty()
    }

    @Test
    fun `verifies parallel loading of similar and genres completes both`() = runTest {
        val filmId = 1L
        val film = mock<ApiFilm>()
        val similar = mock<ApiFilm>().apply {
            whenever(id).thenReturn(1)
            whenever(title).thenReturn("Film")
            whenever(posterPath).thenReturn("poster.jpg")
            whenever(voteAverage).thenReturn("7.5")
            whenever(genreIds).thenReturn(listOf(1))
        }

        var genresCallCount = 0
        var similarCallCount = 0

        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenAnswer {
            similarCallCount++
            listOf(similar)
        }
        whenever(repository.fetchGenres()).thenAnswer {
            genresCallCount++
            mapOf(1 to "Action")
        }

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val similarFilms = viewModel.similarFilm.first()
        assertThat(similarFilms).hasSize(1)
        assertThat(genresCallCount).isEqualTo(1)
        assertThat(similarCallCount).isEqualTo(1)
    }

    @Test
    fun `handles null film id gracefully`() = runTest {
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(null)

        try {
            viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
            advanceUntilIdle()
            assertThat(true).isFalse()
        } catch (e: NullPointerException) {
            assertThat(e).isNotNull()
        }
    }

    @Test
    fun `handles negative film ids`() = runTest {
        val filmId = -1L
        val film = mock<ApiFilm>()
        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(emptyList())
        whenever(repository.fetchGenres()).thenReturn(emptyMap())

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        assertThat(viewModel.filmDetailState.first()).isEqualTo(FilmDetailState.Success(film))
    }

    @Test
    fun `genres failure with partial similar films data`() = runTest {
        val filmId = 2L
        val film = mock<ApiFilm>()
        val similarList = listOf(
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(1)
                whenever(title).thenReturn("Film1")
                whenever(posterPath).thenReturn("p1.jpg")
                whenever(voteAverage).thenReturn("8.0")
                whenever(genreIds).thenReturn(listOf(1, 2))
            },
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(2)
                whenever(title).thenReturn("Film2")
                whenever(posterPath).thenReturn("p2.jpg")
                whenever(voteAverage).thenReturn("7.5")
                whenever(genreIds).thenReturn(listOf(3, 4))
            }
        )

        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(similarList)
        whenever(repository.fetchGenres()).thenThrow(RuntimeException("Genres failed"))

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        val similarFilms = viewModel.similarFilm.first()
        assertThat(similarFilms).hasSize(2)
        assertThat(similarFilms[0].title).isEqualTo("Film1")
        assertThat(similarFilms[1].title).isEqualTo("Film2")
        assertThat(similarFilms[0].genres).isEmpty()
        assertThat(similarFilms[1].genres).isEmpty()
    }

    @Test
    fun `sorts similar films by rating high to low`() = runTest {
        val filmId = 1L
        val film = mock<ApiFilm>()
        val films = listOf(
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(1)
                whenever(title).thenReturn("Low")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("6.0")
                whenever(genreIds).thenReturn(emptyList())
            },
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(2)
                whenever(title).thenReturn("High")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("9.0")
                whenever(genreIds).thenReturn(emptyList())
            },
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(3)
                whenever(title).thenReturn("Mid")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("7.5")
                whenever(genreIds).thenReturn(emptyList())
            }
        )

        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(films)
        whenever(repository.fetchGenres()).thenReturn(emptyMap())

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        viewModel.setSortType(SortType.RATING_HIGH_TO_LOW)
        advanceUntilIdle()

        val sorted = viewModel.sortedSimilarFilms.first()
        assertThat(sorted[0].rating).isEqualTo("9.0")
        assertThat(sorted[1].rating).isEqualTo("7.5")
        assertThat(sorted[2].rating).isEqualTo("6.0")
    }

    @Test
    fun `sorts similar films by title alphabetically`() = runTest {
        val filmId = 1L
        val film = mock<ApiFilm>()
        val films = listOf(
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(1)
                whenever(title).thenReturn("Zebra")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("8.0")
                whenever(genreIds).thenReturn(emptyList())
            },
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(2)
                whenever(title).thenReturn("Apple")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("8.0")
                whenever(genreIds).thenReturn(emptyList())
            },
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(3)
                whenever(title).thenReturn("Mango")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("8.0")
                whenever(genreIds).thenReturn(emptyList())
            }
        )

        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(films)
        whenever(repository.fetchGenres()).thenReturn(emptyMap())

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        viewModel.setSortType(SortType.TITLE_A_TO_Z)
        advanceUntilIdle()

        val sorted = viewModel.sortedSimilarFilms.first()
        assertThat(sorted[0].title).isEqualTo("Apple")
        assertThat(sorted[1].title).isEqualTo("Mango")
        assertThat(sorted[2].title).isEqualTo("Zebra")
    }

    @Test
    fun `default sort maintains original order`() = runTest {
        val filmId = 1L
        val film = mock<ApiFilm>()
        val films = listOf(
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(1)
                whenever(title).thenReturn("First")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("5.0")
                whenever(genreIds).thenReturn(emptyList())
            },
            mock<ApiFilm>().apply {
                whenever(id).thenReturn(2)
                whenever(title).thenReturn("Second")
                whenever(posterPath).thenReturn("p.jpg")
                whenever(voteAverage).thenReturn("9.0")
                whenever(genreIds).thenReturn(emptyList())
            }
        )

        whenever(savedStateHandle.get<Long>(FILM_ID)).thenReturn(filmId)
        whenever(repository.fetchMovie(filmId)).thenReturn(film)
        whenever(repository.fetchSimilar(filmId)).thenReturn(films)
        whenever(repository.fetchGenres()).thenReturn(emptyMap())

        viewModel = FilmDetailViewModel(savedStateHandle, repository, authManager, guestSessionManager)
        advanceUntilIdle()

        viewModel.setSortType(SortType.DEFAULT)
        advanceUntilIdle()

        val sorted = viewModel.sortedSimilarFilms.first()
        assertThat(sorted[0].title).isEqualTo("First")
        assertThat(sorted[1].title).isEqualTo("Second")
    }
}
