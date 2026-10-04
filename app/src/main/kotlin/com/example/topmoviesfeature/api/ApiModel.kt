package com.example.topmoviesfeature.api

import com.google.gson.annotations.SerializedName

data class ApiFilmListResponse(val results: List<ApiFilm>)

data class ApiFilm(
    val id: Long,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>? = null
)

data class ApiGenre(val id: Int, val name: String)

data class ApiGenreListResponse(val genres: List<ApiGenre>)

data class TokenResponse(
    @SerializedName("request_token") val requestToken: String,
    val success: Boolean
)

data class SessionResponse(
    @SerializedName("session_id") val sessionId: String,
    val success: Boolean
)

data class AccountResponse(
    val id: Long,
    @SerializedName("username") val username: String,
    val success: Boolean
)

data class WatchlistRequest(
    val media_type: String = "movie",
    val media_id: Long,
    val watchlist: Boolean
)

data class WatchlistResponse(
    val success: Boolean,
    @SerializedName("status_code") val statusCode: Int? = null,
    @SerializedName("status_message") val statusMessage: String? = null
)

data class GuestSessionResponse(
    @SerializedName("guest_session_id") val guestSessionId: String,
    val success: Boolean
)

data class RatingRequest(
    val value: Float
)

data class RatedMoviesResponse(
    val results: List<RatedMovie>,
    val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

data class RatedMovie(
    val id: Long,
    val rating: Float,
    @SerializedName("vote_average") val voteAverage: Float? = null,
    val title: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null
)

data class RatingResponse(
    val success: Boolean,
    @SerializedName("status_code") val statusCode: Int? = null,
    @SerializedName("status_message") val statusMessage: String? = null
)
