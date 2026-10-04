package biz.filmeroo.premier.detail

import biz.filmeroo.premier.api.ApiFilm
import biz.filmeroo.premier.detail.SimilarFilmMapper.toSimilarFilm
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SimilarFilmTest {

    private var genres = mapOf(1 to "Genre1", 2 to "Genre2")

    private fun createFilm(
        posterPath: String? = "poster",
        voteAvg: String? = "1.22",
        genreIds: List<Int>? = listOf(1, 2)
    ) =
        ApiFilm(
            id = 1,
            title = "title",
            overview = "",
            posterPath = posterPath,
            backdropPath = null,
            voteAverage = voteAvg,
            genreIds = genreIds
        )

    @Test
    fun ` map genre ids to names joined with bullet`() {
        val result = createFilm().toSimilarFilm(genres)
        assertThat(result.genres).isEqualTo("Genre1 * Genre2")
    }

    @Test
    fun `rating with one decimal place`() {
        assertThat(createFilm().toSimilarFilm(genres).rating).isEqualTo("1.2")
    }

    @Test
    fun `genre ids is null`() {
        assertThat(createFilm(genreIds = null).toSimilarFilm(genres).genres).isEmpty()
    }

    @Test
    fun `genre ids are not present`() {
        genres = mapOf(11 to "Genre1", 22 to "Genre2")
        assertThat(createFilm(genreIds = null).toSimilarFilm(genres).genres).isEmpty()
    }

    @Test
    fun `takes only two genres`() {
        genres = mapOf(1 to "Genre11", 2 to "Genre22",3 to "Genre33", 4 to "Genre44")
        assertThat(createFilm().toSimilarFilm(genres).genres).isEqualTo("Genre11 * Genre22")
    }

    @Test
    fun `rating is not a valid integer`() {
        assertThat(createFilm(voteAvg = "dummy").toSimilarFilm(genres).rating).isEmpty()
    }

    @Test
    fun `handles null poster path gracefully`() {
        val result = createFilm(posterPath = null).toSimilarFilm(genres)
        assertThat(result.posterPath).isNull()
        assertThat(result.title).isEqualTo("title")
    }

    @Test
    fun `handles null vote average`() {
        val result = createFilm(voteAvg = null).toSimilarFilm(genres)
        assertThat(result.rating).isEmpty()
    }

    @Test
    fun `handles empty genre ids list`() {
        val result = createFilm(genreIds = emptyList()).toSimilarFilm(genres)
        assertThat(result.genres).isEmpty()
    }

    @Test
    fun `handles genre ids partially in map`() {
        val result = createFilm(genreIds = listOf(1, 999)).toSimilarFilm(genres)
        assertThat(result.genres).isEqualTo("Genre1")
    }

    @Test
    fun `handles single genre id`() {
        val result = createFilm(genreIds = listOf(1)).toSimilarFilm(genres)
        assertThat(result.genres).isEqualTo("Genre1")
    }

    @Test
    fun `handles empty genre map`() {
        val emptyGenres = emptyMap<Int, String>()
        val result = createFilm().toSimilarFilm(emptyGenres)
        assertThat(result.genres).isEmpty()
    }

    @Test
    fun `handles rating with zero value`() {
        val result = createFilm(voteAvg = "0.0").toSimilarFilm(genres)
        assertThat(result.rating).isEqualTo("0.0")
    }

    @Test
    fun `handles rating with high value`() {
        val result = createFilm(voteAvg = "9.99").toSimilarFilm(genres)
        assertThat(result.rating).isEqualTo("10.0")
    }

    @Test
    fun `handles rating with single digit`() {
        val result = createFilm(voteAvg = "5").toSimilarFilm(genres)
        assertThat(result.rating).isEqualTo("5.0")
    }

    @Test
    fun `preserves film id during transformation`() {
        val film = ApiFilm(
            id = 12345,
            title = "title",
            overview = "",
            posterPath = "poster",
            backdropPath = null,
            voteAverage = "1.22",
            genreIds = listOf(1, 2)
        )
        val result = film.toSimilarFilm(genres)
        assertThat(result.id).isEqualTo(12345)
    }

    @Test
    fun `preserves film title during transformation`() {
        val result = createFilm().toSimilarFilm(genres)
        assertThat(result.title).isEqualTo("title")
    }

    @Test
    fun `handles genre ids with duplicates`() {
        val result = createFilm(genreIds = listOf(1, 1, 2, 2)).toSimilarFilm(genres)
        assertThat(result.genres).isEqualTo("Genre1 * Genre1")
    }

    @Test
    fun `handles extremely high rating value`() {
        val result = createFilm(voteAvg = "99.9").toSimilarFilm(genres)
        assertThat(result.rating).isEqualTo("99.9")
    }

    @Test
    fun `handles negative rating value`() {
        val result = createFilm(voteAvg = "-5.5").toSimilarFilm(genres)
        assertThat(result.rating).isEqualTo("-5.5")
    }
}
