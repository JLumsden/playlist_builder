package com.example.playlist_builder.service;

import com.example.playlist_builder.data.CreatePlaylistDto;
import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.repository.SpotifyApiRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SpotifyApiService {
    private final SpotifyApiRepository spotifyApiRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String createPlaylist(String accessToken, Setlist setlist) {
        CreatePlaylistDto createPlaylistDto = setCreatePlaylistDto(setlist);

        HttpEntity<String> entity = new HttpEntity<>(createPlaylistDtoToJson(createPlaylistDto), createHeaders(accessToken));

        String url = createPlaylistUrlBuilder(getUserId(accessToken));

        return spotifyApiRepository.post(url, entity, String.class).getBody();
    }

    public String getUserId(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.get(url, entity, String.class);

        return parseUserProfileForId(response.getBody());
    }

    public String parseUserProfileForId(String profileJson) {
        try
        {
            JsonNode rootNode = objectMapper.readTree(profileJson);
            return rootNode.path("id").asText();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public CreatePlaylistDto setCreatePlaylistDto(Setlist setlist) {
        return new CreatePlaylistDto(setlist.getArtist() + "Setlist",
                "Setlist for " + setlist.getArtist() + "'s show");
    }

    public HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        return headers;
    }

    public String createPlaylistDtoToJson(CreatePlaylistDto createPlaylistDto) {
        try {
            return objectMapper.writeValueAsString(createPlaylistDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String createPlaylistUrlBuilder(String userId) {
        return "https://api.spotify.com/v1/users/" + userId + "/playlists";
    }
}
