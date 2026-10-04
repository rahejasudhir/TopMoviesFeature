package biz.filmeroo.premier.api

import retrofit2.http.GET
import retrofit2.http.Path

interface FilmService {

    @GET("movie/top_rated")
    suspend fun topRated(): ApiFilmListResponse

    @GET("movie/{movie_id}")
    suspend fun movie(@Path("movie_id") movieId: Long): ApiFilm

    @GET("movie/{movie_id}/similar")
    suspend fun similar(@Path("movie_id") movieId: Long): ApiFilmListResponse

    @GET("genre/movie/list")
    suspend fun genres(): ApiGenreListResponse

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY_PARAM = "api_key"

        fun buildImageUrl(path: String) = "https://image.tmdb.org/t/p/w500$path"
    }
}
