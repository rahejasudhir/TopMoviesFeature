package com.example.topmoviesfeature.detail

import com.example.topmoviesfeature.api.ApiFilm
import java.util.Locale

object SimilarFilmMapper {
    fun ApiFilm.toSimilarFilm(genreNames: Map<Int, String>) = SimilarFilm(
        id = id,
        title = title,
        posterPath = posterPath,
        genres = genreIds.orEmpty().mapNotNull(genreNames::get).take(2)
            .joinToString(" * "),
        rating = voteAverage?.toDoubleOrNull()
            ?.let { String.format(Locale.getDefault(), "%.1f", it) }.orEmpty()
    )
}
