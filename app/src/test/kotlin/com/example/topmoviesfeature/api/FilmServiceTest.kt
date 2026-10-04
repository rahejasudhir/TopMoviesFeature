package biz.filmeroo.com.example.topmoviesfeature.api

import com.example.topmoviesfeature.api.FilmService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FilmServiceTest {

    @Test
    fun `formats image url`() {
        val formattedUrl = FilmService.Companion.buildImageUrl("/test.png")

        assertThat(formattedUrl).isEqualTo("https://image.tmdb.org/t/p/w500/test.png")
    }
}
