package com.example.playlist_builder.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePlaylistDto {
    private String name;
    private String description;
}
