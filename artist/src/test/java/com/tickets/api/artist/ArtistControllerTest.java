package com.tickets.api.artist;

import com.tickets.api.artist.dto.Artist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ArtistController.class)
public class ArtistControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ArtistService artistService;

    @Test
    public void testArtistById() {
        Artist artist = new Artist();
        artist.setId(21L);
        artist.setName("HRH Prog");
        artist.setRank(1);
        artist.setUrl("/hrh-prog-tickets/artist/21");
        artist.setImgSrc("//some-base-url/hrh-prog.jpg");
        when(artistService.findById(21L)).thenReturn(Mono.just(artist));
        this.webClient.get()
                .uri("/artist/21").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Artist.class)
                .isEqualTo(artist);
    }

    @Test
    public void testArtistByIdWithInvalidArtistIdAndExpect404() {
        when(artistService.findById(1L)).thenReturn(Mono.error(new RuntimeException(HttpStatus.NOT_FOUND.toString())));
        this.webClient.get()
                .uri("/artist/1").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

}