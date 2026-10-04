package biz.filmeroo.premier.main

import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.api.FilmService
import javax.inject.Inject

internal class FilmRepository @Inject constructor(private val filmService: FilmService) {

    suspend fun fetchTopRated(): List<ApiFilm> {
        return filmService.topRated().results
    }

    suspend fun fetchMovie(id: Long): ApiFilm {
        return filmService.movie(id)
    }

    suspend fun fetchSimilar(id: Long): List<ApiFilm> {
        return filmService.similar(id).results
    }

    suspend fun fetchGenres(): Map<Int, String> {
        return filmService.genres().genres.associate { it.id to it.name }
    }
}
