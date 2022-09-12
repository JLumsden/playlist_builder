package com.example.playlist_builder.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Track {
    private int popularity;
    private String uri;
}
