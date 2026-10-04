package biz.filmeroo.premier.api

import com.google.gson.annotations.SerializedName

data class ApiFilmListResponse(val results: List<ApiFilm>)

data class ApiFilm(
    val id: Long,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: String?
)
