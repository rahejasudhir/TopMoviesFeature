package com.example.topmoviesfeature.main

import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.api.FilmService
import javax.inject.Inject

internal class FilmRepository @Inject constructor(private val filmService: FilmService) {

    suspend fun fetchTopRated(): List<ApiFilm> {
        return filmService.topRated().results
    }

    suspend fun fetchMovie(id: Long): ApiFilm {
        return filmService.movie(id)
    }

    suspend fun search(query: String): List<ApiFilm> {
        return filmService.search(query).results
    }
}
