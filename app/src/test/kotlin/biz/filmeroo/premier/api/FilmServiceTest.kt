package biz.filmeroo.premier.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FilmServiceTest {

    @Test
    fun `formats image url`() {
        val formattedUrl = FilmService.buildImageUrl("/test.png")

        assertThat(formattedUrl).isEqualTo("https://image.tmdb.org/t/p/w500/test.png")
    }
}
