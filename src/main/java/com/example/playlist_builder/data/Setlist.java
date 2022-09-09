package com.example.playlist_builder.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Setlist {
    private String setlistId;
    private String name;
    private String artist;
    private List<String> songNames;
}
