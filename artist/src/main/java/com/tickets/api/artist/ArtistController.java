package com.tickets.api.artist;

import com.tickets.api.artist.dto.Artist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
@RestController
public class ArtistController {

    private ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping(value = "${paths.artist}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Artist>> artistById(@PathVariable Long id) {
        log.info("Inside ArtistController. Fetching artist by id {}", id);
        return artistService.findById(id)
                .filter(Objects::nonNull)
                .map(artist -> ResponseEntity.ok(artist))
                .doOnError(error -> {
                    if (HttpStatus.NOT_FOUND.toString().equalsIgnoreCase(error.getMessage())) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
                    } else if (HttpStatus.INTERNAL_SERVER_ERROR.toString().equalsIgnoreCase(error.getMessage())) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

}
