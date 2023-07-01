package com.tickets.api.artist;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.tickets.api.artist.dto.Artist;
import com.tickets.api.artist.dto.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.util.Arrays.asList;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ArtistController.class)
@Import(ArtistService.class)
public class ArtistIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8085);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8085);

        stubFor(get("/artist.json")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(buildArtist())
                ));

        stubFor(get("/events.json")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(buildEvent())
                ));
    }

    @Test
    public void testArtistById() {
        Artist artist = new Artist();
        artist.setId(21L);
        artist.setName("HRH Prog");
        artist.setRank(1);
        artist.setUrl("/hrh-prog-tickets/artist/21");
        artist.setImgSrc("//some-base-url/hrh-prog.jpg");

        Events events1 = new Events();
        events1.setId(1L);
        events1.setTitle("Fusion Prog");
        events1.setDateStatus("singleDate");
        events1.setTimeZone("Europe/London");
        events1.setStartDate(LocalDateTime.of(2020, 10, 17, 00, 00));
        events1.setHiddenFromSearch(false);

        Events events2 = new Events();
        events2.setId(7L);
        events2.setTitle("A festival Live");
        events2.setDateStatus("singleDate");
        events2.setTimeZone(null);
        events2.setStartDate(null);
        events2.setHiddenFromSearch(false);

        Events events3 = new Events();
        events3.setId(13L);
        events3.setTitle("Huge Live");
        events3.setDateStatus("multiDate");
        events3.setTimeZone(null);
        events3.setStartDate(null);
        events3.setHiddenFromSearch(false);

        artist.setEvents(asList(events1, events2, events3));

        this.webClient.get()
                .uri("/artist/21").accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Artist.class)
                .isEqualTo(artist);
    }

    private String buildEvent() {
        return "[{\"title\":\"Fusion Prog\",\"id\":\"1\",\"dateStatus\":\"singleDate\",\"timeZone\":\"Europe/London\",\"startDate\":\"2020-10-17T00:00:00\"," +
                "\"artists\":[{\"id\":\"21\"},{\"id\":\"23\"},{\"id\":\"26\"}],\"venue\":{\"id\":\"41\"},\"hiddenFromSearch\":false}," +
                "{\"title\":\"Blues In Space\",\"id\":\"2\",\"dateStatus\":\"singleDate\",\"timeZone\":\"Europe/London\",\"" +
                "startDate\":\"2020-10-18T00:00:00\",\"artists\":[{\"id\":\"22\"},{\"id\":\"25\"},{\"id\":\"27\"}],\"venue\":{\"id\":\"42\"}," +
                "\"hiddenFromSearch\":false},{\"title\":\"Jazz Live\",\"id\":\"3\",\"dateStatus\":\"singleDate\"," +
                "\"artists\":[{\"id\":\"24\"},{\"id\":\"28\"},{\"id\":\"29\"}],\"venue\":{\"id\":\"43\"},\"hiddenFromSearch\":true}," +
                "{\"title\":\"Metal Event\",\"id\":\"4\",\"dateStatus\":\"multiDate\",\"artists\":[{\"id\":\"24\"}],\"venue\":{\"id\":\"44\"}," +
                "\"hiddenFromSearch\":false},{\"title\":\"Pop Event\",\"id\":\"5\",\"dateStatus\":\"none\",\"artists\":[{\"id\":\"26\"}],\"venue\":{\"id\":\"42\"}," +
                "\"hiddenFromSearch\":false},{\"title\":\"Dance Live\",\"id\":\"6\",\"dateStatus\":\"singleDate\",\"artists\":[{\"id\":\"28\"}],\"venue\":{\"id\":\"45\"}," +
                "\"hiddenFromSearch\":false},{\"title\":\"A festival Live\",\"id\":\"7\",\"dateStatus\":\"singleDate\",\"artists\":[{\"id\":\"21\"},{\"id\":\"22\"}]," +
                "\"venue\":{\"id\":\"45\"}},{\"title\":\"Trip Hop\",\"id\":\"8\",\"dateStatus\":\"singleDate\",\"artists\":[{\"id\":\"100\"}],\"venue\":{\"id\":\"41\"}," +
                "\"hiddenFromSearch\":false},{\"title\":\"Another one\",\"id\":\"9\",\"dateStatus\":\"singleDate\",\"artists\":[{\"id\":\"25\"},{\"id\":\"29\"},{\"id\":\"29\"}]," +
                "\"venue\":{\"id\":\"40\"},\"hiddenFromSearch\":false},{\"title\":\"An Event\",\"id\":\"10\",\"dateStatus\":\"multiDate\",\"artists\":[{\"id\":\"24\"}]," +
                "\"venue\":{\"id\":\"42\"},\"hiddenFromSearch\":false},{\"title\":\"Harisson Live\",\"id\":\"11\",\"dateStatus\":\"singleDate\",\"artists\":[{\"id\":\"22\"}," +
                "{\"id\":\"23\"},{\"id\":\"24\"}],\"venue\":{\"id\":\"44\"},\"hiddenFromSearch\":false},{\"title\":\"Paranoid Live\",\"id\":\"12\",\"dateStatus\":\"none\"," +
                "\"artists\":[{\"id\":\"27\"}],\"venue\":{\"id\":\"45\"},\"hiddenFromSearch\":false},{\"title\":\"Huge Live\",\"id\":\"13\"," +
                "\"dateStatus\":\"multiDate\",\"artists\":[{\"id\":\"21\"},{\"id\":\"22\"},{\"id\":\"24\"},{\"id\":\"28\"},{\"id\":\"29\"}]," +
                "\"venue\":{\"id\":\"41\"},\"hiddenFromSearch\":false}]";
    }

    private String buildArtist() {
        return "[{\"name\":\"HRH Prog\",\"id\":\"21\",\"imgSrc\":\"//some-base-url/hrh-prog.jpg\",\"url\":\"/hrh-prog-tickets/artist/21\",\"rank\":1}," +
                "{\"name\":\"Colosseum\",\"id\":\"22\",\"imgSrc\":\"//some-base-url/colosseum.jpg\",\"url\":\"/colosseum-tickets/artist/22\",\"rank\":2}," +
                "{\"name\":\"Mostly Autumn\",\"id\":\"23\",\"imgSrc\":\"//some-base-url/mostly-autumn.jpg\",\"url\":\"/mostly-autumn-tickets/artist/23\",\"rank\":3}," +
                "{\"name\":\"The Enid\",\"id\":\"24\",\"imgSrc\":\"//some-base-url/the-enid.jpg\",\"url\":\"/the-enid-tickets/artist/24\",\"rank\":4}," +
                "{\"name\":\"Threshold\",\"id\":\"25\",\"imgSrc\":\"//some-base-url/threshold.jpg\",\"url\":\"/threshold-tickets/artist/25\",\"rank\":5}," +
                "{\"name\":\"Ozric Tentacles\",\"id\":\"26\",\"imgSrc\":\"//some-base-url/ozric-tentacles.jpg\",\"url\":\"/ozric-tentacles-tickets/artist/26\",\"rank\":6}," +
                "{\"name\":\"The Crazy World of Arthur Brown\",\"id\":\"27\",\"imgSrc\":\"//some-base-url/arthur-brown.jpg\"," +
                "\"url\":\"/the-crazy-world-of-arthur-brown-tickets/artist/27\",\"rank\":7},{\"name\":\"Moon Safari\",\"id\":\"28\"," +
                "\"imgSrc\":\"//some-base-url/moon-safari.jpg\",\"url\":\"/moon-safari-tickets/artist/28\",\"rank\":8},{\"name\":\"Magenta\",\"id\":\"29\"," +
                "\"imgSrc\":\"//some-base-url/magenta.jpg\",\"url\":\"/magenta-tickets/artist/29\",\"rank\":9}]";
    }

}