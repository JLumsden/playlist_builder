package com.example.playlist_builder.service;

import com.example.playlist_builder.data.CreatePlaylistDto;
import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.data.SonglistDto;
import com.example.playlist_builder.repository.SpotifyApiRepository;
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
    private final JsonService jsonService;

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
                uri = jsonService.parseSearchResultForUri(response.getBody(), setlist.getArtist());
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

        String payload = jsonService.dtoToJson(songlistDto);

        HttpEntity<String> entity = new HttpEntity<>(payload, createHeaders(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.post(url, entity, String.class);

        return response;
    }

    public String createPlaylist(Setlist setlist, String accessToken) {
        CreatePlaylistDto createPlaylistDto = setCreatePlaylistDto(setlist);

        HttpEntity<String> entity = new HttpEntity<>(jsonService.dtoToJson(createPlaylistDto), createHeaders(accessToken));

        String url = createPlaylistUrlBuilder(getUserId(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.post(url, entity, String.class);
        if(response.getStatusCodeValue() != 201) {
            return "error";
        }
        return jsonService.parseForId(response.getBody());
    }

    public String getUserId(String accessToken) {
        String url = "https://api.spotify.com/v1/me";

        HttpEntity<Void> entity = new HttpEntity<>(createHeaders(accessToken));

        ResponseEntity<String> response = spotifyApiRepository.get(url, entity, String.class);

        return jsonService.parseForId(response.getBody());
    }

    public CreatePlaylistDto setCreatePlaylistDto(Setlist setlist) {
        return new CreatePlaylistDto(setlist.getArtist() + " " + setlist.getName(),
                "Setlist for " + setlist.getArtist() + "'s show");
    }

    public HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        return headers;
    }

    public String createPlaylistUrlBuilder(String userId) {
        return "https://api.spotify.com/v1/users/" + userId + "/playlists";
    }
}
