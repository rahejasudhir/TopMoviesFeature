package com.example.topmoviesfeature.main

import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.api.FilmService
import javax.inject.Inject

internal class FilmRepository @Inject constructor(private val filmService: FilmService) {

    suspend fun fetchTopRated(page: Int = 1): List<ApiFilm> {
        return filmService.topRated(page).results
    }

    suspend fun fetchMovie(id: Long): ApiFilm {
        return filmService.movie(id)
    }

    suspend fun search(query: String, page: Int = 1): List<ApiFilm> {
        return filmService.search(query, page).results
    }

    suspend fun similar(movieId: Long, page: Int = 1): List<ApiFilm> {
        return filmService.similar(movieId, page).results
    }
}
