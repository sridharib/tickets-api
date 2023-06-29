package com.tickets.api.artist;

import com.tickets.api.artist.dto.Artist;
import com.tickets.api.artist.dto.Events;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;

import static java.util.Objects.isNull;

@Slf4j
@Service
public class ArtistService {

    private final WebClient webClient;

    private String artistURI;

    private String eventsURI;

    public ArtistService(@Value("${url.base}") String baseURL, @Value("${url.artist}") String artistURI, @Value("${url.events}") String eventsURI) {
        this.webClient = WebClient.builder().baseUrl(baseURL).build();
        this.artistURI = artistURI;
        this.eventsURI = eventsURI;
    }

    public Mono<Artist> findById(Long id) {
        log.info("fetching artist by id {}", id);
        return webClient.get()
                .uri(artistURI)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, error -> Mono.error(new RuntimeException(HttpStatus.INTERNAL_SERVER_ERROR.toString())))
                .bodyToFlux(Artist.class)
                .filter(artist -> Objects.equals(id, artist.getId()))
                .single(new Artist())
                .flatMap(artist ->
                        findEventsByArtistId(artist.getId())
                                .collectList()
                                .map(events -> {
                                    artist.setEvents(events);
                                    return artist;
                                }))
                .handle((artist, sink) -> {
                    if (isNull(artist.getId())) {
                        sink.error(new RuntimeException(HttpStatus.NOT_FOUND.toString()));
                    } else {
                        sink.next(artist);
                    }
                });
    }

    public Flux<Events> findEventsByArtistId(Long artistId) {
        log.info("fetching events by artist id {}", artistId);
        return webClient.get()
                .uri(eventsURI)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, error -> Mono.error(new RuntimeException(HttpStatus.INTERNAL_SERVER_ERROR.toString())))
                .bodyToFlux(Events.class)
                .filter(events -> Arrays.stream(events.getArtists()).anyMatch(artist -> Objects.equals(artistId, artist.getId())))
                .map(events -> {
                    events.setArtists(null);
                    return events;
                });
    }

}
