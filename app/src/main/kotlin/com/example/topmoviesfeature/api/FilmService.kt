package com.example.topmoviesfeature.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FilmService {

    @GET("movie/top_rated")
    suspend fun topRated(): ApiFilmListResponse

    @GET("movie/{movie_id}")
    suspend fun movie(@Path("movie_id") movieId: Long): ApiFilm

    @GET("search/movie")
    suspend fun search(@Query("query") query: String): ApiFilmListResponse

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY_PARAM = "api_key"

        fun buildImageUrl(path: String) = "https://image.tmdb.org/t/p/w500$path"
    }
}
