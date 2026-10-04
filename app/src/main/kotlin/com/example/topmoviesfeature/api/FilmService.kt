package com.example.topmoviesfeature.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FilmService {

    @GET("movie/top_rated")
    suspend fun topRated(): ApiFilmListResponse

    @GET("movie/{movie_id}")
    suspend fun movie(@Path("movie_id") movieId: Long): ApiFilm

    @GET("movie/{movie_id}/similar")
    suspend fun similar(@Path("movie_id") movieId: Long): ApiFilmListResponse

    @GET("genre/movie/list")
    suspend fun genres(): ApiGenreListResponse

    @POST("authentication/token/new")
    suspend fun getRequestToken(): TokenResponse

    @POST("authentication/session/new")
    suspend fun createSession(
        @Query("request_token") requestToken: String
    ): SessionResponse

    @GET("account")
    suspend fun getAccount(@Query("session_id") sessionId: String): AccountResponse

    @POST("account/{account_id}/watchlist")
    suspend fun addToWatchlist(
        @Path("account_id") accountId: Long,
        @Query("session_id") sessionId: String,
        @Body request: WatchlistRequest
    ): WatchlistResponse

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY_PARAM = "api_key"
        const val TMDB_AUTH_URL = "https://www.themoviedb.org/authenticate/"

        fun buildImageUrl(path: String) = "https://image.tmdb.org/t/p/w500$path"
        fun buildAuthUrl(requestToken: String) = "$TMDB_AUTH_URL$requestToken"
    }
}
