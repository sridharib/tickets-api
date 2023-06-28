package com.tickets.api.artist;

import com.tickets.api.artist.dto.Artist;
import com.tickets.api.artist.dto.Events;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtistServiceTest {

    private MockWebServer mockWebServer;

    private ArtistService artistService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        HttpUrl url = mockWebServer.url("/api");
        mockWebServer.start();
        artistService = new ArtistService(WebClient.builder(), url.url().toString(), "/artist.json", "/events.json");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldReturnArtistWhenFindByIdIsCalled() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{\"id\":21,\"name\":\"HRH Prog\",\"imgSrc\":\"//some-base-url/hrh-prog.jpg\",\"url\":\"/hrh-prog-tickets/artist/21\",\"rank\":1}")
        );
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(buildEvent())
        );

        // When
        Mono<Artist> artistMono = artistService.findById(21L);

        // Then
        StepVerifier.create(artistMono)
                .expectNextMatches(artist -> artist.getId().equals(21L)
                        && artist.getName().equals("HRH Prog")
                        && artist.getEvents().size() == 3
                        && artist.getEvents().stream().anyMatch(events -> events.getId().equals(1L)))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/artist.json", recordedRequest.getPath());

        recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/events.json", recordedRequest.getPath());
    }

    @Test
    void shouldThrowExceptionWhenArtistIsNotFound() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{\"id\":21,\"name\":\"HRH Prog\",\"imgSrc\":\"//some-base-url/hrh-prog.jpg\",\"url\":\"/hrh-prog-tickets/artist/21\",\"rank\":1}")
        );
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(buildEvent())
        );

        // When
        Mono<Artist> artistMono = artistService.findById(200L);

        // Then
        StepVerifier.create(artistMono)
                .expectErrorMatches(error -> error instanceof RuntimeException && error.getMessage().equals(HttpStatus.NOT_FOUND.toString()))
                .verify();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/artist.json", recordedRequest.getPath());

        recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/events.json", recordedRequest.getPath());
    }

    @Test
    void shouldReturnEventForArtistsWhenFindEventsByArtistIdIsCalled() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(buildEvent())
        );

        // When
        Flux<Events> eventsByArtistId = artistService.findEventsByArtistId(21L);

        // Then
        StepVerifier.create(eventsByArtistId)
                .expectNextMatches(events -> events.getId().equals(1L))
                .expectNextMatches(events -> events.getId().equals(7L))
                .expectNextMatches(events -> events.getId().equals(13L))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/events.json", recordedRequest.getPath());
    }

    @Test
    void shouldReturnEmptyEventWhenFindEventsByArtistIdIsCalledWithUnmatchedArtistId() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(buildEvent())
        );

        // When
        Flux<Events> eventsByArtistId = artistService.findEventsByArtistId(200L);

        // Then
        StepVerifier.create(eventsByArtistId)
                .expectNextCount(0)
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/api/events.json", recordedRequest.getPath());
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

}