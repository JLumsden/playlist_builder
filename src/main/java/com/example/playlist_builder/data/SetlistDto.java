package com.example.playlist_builder.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SetlistDto {
    private String artist;
    private List<String> songNames;
}
