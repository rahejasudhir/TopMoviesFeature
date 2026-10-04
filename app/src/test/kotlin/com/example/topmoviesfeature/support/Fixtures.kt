package biz.filmeroo.com.example.topmoviesfeature.support

import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.api.ApiFilmListResponse

object Fixtures {

    fun film(id: Long): ApiFilm {
        return ApiFilm(
            id = id,
            title = "Waterworld",
            overview = "Kevin Costner on a boat",
            posterPath = "/123.jpg",
            backdropPath = "/456.jpg",
            voteAverage = "5.5"
        )
    }

    fun filmList(): List<ApiFilm> {
        return listOf(film(123), film(456))
    }

    fun filmResponse(): ApiFilmListResponse {
        return ApiFilmListResponse(filmList())
    }
}
