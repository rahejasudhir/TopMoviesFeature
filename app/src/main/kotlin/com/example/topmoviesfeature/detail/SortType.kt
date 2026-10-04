package com.example.topmoviesfeature.detail

enum class SortType {
    DEFAULT,
    RATING_HIGH_TO_LOW,
    TITLE_A_TO_Z;

    fun sort(films: List<SimilarFilm>): List<SimilarFilm> {
        return when (this) {
            DEFAULT -> films
            RATING_HIGH_TO_LOW -> {
                val mutable = films.toMutableList()
                mutable.sortByDescending { it.rating.toDoubleOrNull() ?: 0.0 }
                mutable
            }
            TITLE_A_TO_Z -> {
                val mutable = films.toMutableList()
                mutable.sortBy { it.title }
                mutable
            }
        }
    }
}
