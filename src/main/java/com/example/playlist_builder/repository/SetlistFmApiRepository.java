package com.example.playlist_builder.repository;

import com.example.playlist_builder.config.SetlistFmConfig;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
public class SetlistFmApiRepository {
    private final RestTemplate restTemplate = new RestTemplate();

    public <T, R> ResponseEntity<T> get(String url, HttpEntity<R> entity, Class<T> clazz) {
        return restTemplate.exchange(url, HttpMethod.GET, entity, clazz);
    }
}
