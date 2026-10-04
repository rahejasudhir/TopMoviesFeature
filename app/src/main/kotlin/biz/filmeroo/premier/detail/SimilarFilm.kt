package biz.filmeroo.premier.detail

data class SimilarFilm(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val genres: String,
    val rating: String
)
