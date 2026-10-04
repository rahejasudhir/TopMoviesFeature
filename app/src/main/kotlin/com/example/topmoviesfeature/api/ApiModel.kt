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
