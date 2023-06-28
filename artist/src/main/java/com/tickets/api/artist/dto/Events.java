package com.tickets.api.artist.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Events {

    private Long id;

    private String title;

    private String dateStatus;

    private String timeZone;

    private LocalDateTime startDate;

    private Artist[] artists;

    //private Venue venue;

    private boolean hiddenFromSearch;

}
