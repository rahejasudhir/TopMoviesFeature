package biz.filmeroo.premier.support

import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.api.ApiFilmListResponse
import biz.filmeroo.premier.api.ApiGenre
import biz.filmeroo.premier.api.ApiGenreListResponse

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

    fun genre(id: Int, name: String = "Genre"): ApiGenre {
        return ApiGenre(id = id, name = name)
    }

    fun genreList(): List<ApiGenre> {
        return listOf(
            genre(1, "Action"),
            genre(2, "Comedy"),
            genre(3, "Drama")
        )
    }

    fun genreResponse(): ApiGenreListResponse {
        return ApiGenreListResponse(genreList())
    }
}
