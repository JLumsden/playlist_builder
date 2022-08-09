package com.example.playlist_builder.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
@Data
public class SetlistFmConfig {
    private final String token = /*api key here*/;
}
