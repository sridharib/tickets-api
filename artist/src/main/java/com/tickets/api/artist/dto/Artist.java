package com.tickets.api.artist.dto;

import lombok.Data;

import java.util.List;

@Data
public class Artist {

    private Long id;

    private String name;

    private String imgSrc;

    private String url;

    private long rank;

    private List<Events> events;

}
