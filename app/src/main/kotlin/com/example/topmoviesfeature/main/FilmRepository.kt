package com.example.topmoviesfeature.main

import com.example.topmoviesfeature.api.ApiFilm
import com.example.topmoviesfeature.api.FilmService
import com.example.topmoviesfeature.api.WatchlistRequest
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

    suspend fun getRequestToken(): String {
        return filmService.getRequestToken().requestToken
    }

    suspend fun createSession(requestToken: String): String {
        return filmService.createSession(requestToken).sessionId
    }

    suspend fun getAccount(sessionId: String): Pair<Long, String> {
        val account = filmService.getAccount(sessionId)
        return account.id to account.username
    }

    suspend fun addToWatchlist(
        accountId: Long,
        sessionId: String,
        filmId: Long,
        add: Boolean
    ): Boolean {
        return filmService.addToWatchlist(
            accountId,
            sessionId,
            WatchlistRequest(media_id = filmId, watchlist = add)
        ).success
    }
}
