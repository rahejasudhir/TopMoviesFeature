package com.example.topmoviesfeature.detail

data class SimilarFilm(
    val id: Long,
    val title: String,
    val posterPath: String?,
    val genres: String,
    val rating: String
)
