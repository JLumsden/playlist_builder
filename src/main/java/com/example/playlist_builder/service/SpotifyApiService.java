package com.example.playlist_builder.service;

import com.example.playlist_builder.data.CreatePlaylistDto;
import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.data.SonglistDto;
import com.example.playlist_builder.repository.SpotifyApiRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SpotifyApiService {
    private final SpotifyApiRepository spotifyApiRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String buildPlaylistDelegator(Setlist setlist, String accessToken) {
        SonglistDto songlistDto = createSonglistDto(setlist, accessToken);

        String playlistId = createPlaylist(setlist, accessToken);
        if (playlistId.equals("error")) {
            return playlistId;
        }

        ResponseEntity<String> response = populatePlaylist(songlistDto, playlistId, accessToken);
        if (response.getStatusCodeValue() != 201) {
            return "error";
        }

        return "playlist_built_success";
    }

    public SonglistDto createSonglistDto(Setlist setlist, String accessToken) throws HttpClientErrorException {
        List<String> songlist = new ArrayList<>();
        String artist = setlist.getArtist();
        String url, uri, q;
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(accessToken));
        ResponseEntity<String> response;

        for (String song : setlist.getSongNames()) {
            q = artist + " " + song;
            url = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + "&type=track";
            log.info("URL: " + url);

            response = spotifyApiRepository.get(url, entity, String.class);

            if (response.getStatusCodeValue() != 200) {
                log.info("Failed to search: " + song);
                throw new RuntimeException();
            } else {
                uri = parseForUri(response.getBody(), setlist.getArtist());
                if (!(uri.equals("error"))) {
                    songlist.add(uri);
                    log.info("Track: " + song);
                    log.info("Uri: " + uri);
                } else {
                    log.info("Failed to parse for track: " + song);
                }
            }
        }
        return new SonglistDto(songlist);
    }

    public ResponseEntity<String> populatePlaylist(SonglistDto songlistDto, String playlistId, String accessToken) {
        String url = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";

        String payload = dtoToJson(songlistDto);

        HttpEntity<String> entity = new HttpEntity<>(payload, createHeaders(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.post(url, entity, String.class);

        return response;
    }

    public String createPlaylist(Setlist setlist, String accessToken) {
        CreatePlaylistDto createPlaylistDto = setCreatePlaylistDto(setlist);

        HttpEntity<String> entity = new HttpEntity<>(dtoToJson(createPlaylistDto), createHeaders(accessToken));

        String url = createPlaylistUrlBuilder(getUserId(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.post(url, entity, String.class);
        if(response.getStatusCodeValue() != 201) {
            return "error";
        }
        return parseForId(response.getBody());
    }

    public String getUserId(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.get(url, entity, String.class);

        return parseForId(response.getBody());
    }

    public String parseForId(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            return rootNode.path("id").asText();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String parseForUri(String json, String artist) {
        try {
            JsonNode artistsNode;
            JsonNode rootNode = objectMapper.readTree(json);
            rootNode = rootNode.path("tracks");
            rootNode = rootNode.path("items");
            for (JsonNode trackNode : rootNode) {
                //TODO THIS IS SO BAD. DO BETTER
                artistsNode = trackNode.path("artists");
                for (JsonNode artistNode : artistsNode) {
                    if (artistNode.path("name").asText().equals(artist)) {
                        return trackNode.path("uri").asText();
                    }
                }
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return "error";
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

    public <T> String dtoToJson(T dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String createPlaylistUrlBuilder(String userId) {
        return "https://api.spotify.com/v1/users/" + userId + "/playlists";
    }
}
