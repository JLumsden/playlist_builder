package com.example.playlist_builder.service;

import com.example.playlist_builder.config.SetlistFmConfig;
import com.example.playlist_builder.data.Playlist;
import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.repository.PlaylistRepository;
import com.example.playlist_builder.repository.SetlistFmApiRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class SetlistFmService {
    private SetlistFmApiRepository setlistFmApiRepository;
    private SetlistFmConfig setlistFmConfig;
    private ObjectMapper objectMapper;
    private PlaylistRepository playlistRepository;

    public String postPlaylistUrlDelegator(Setlist setlist, String setlistUrl) {
        log.info("SetlistUrl: " + setlistUrl);

        String setlistId = setlistIdParser(setlistUrl);
        if (setlistId.equals("error handling should be better")) {
            return "error";
        }
        setlist.setSetlistId(setlistId);

        ResponseEntity<String> response = getSetlistItems(setlistId);
        if (response.getStatusCodeValue() != 200) {
            return "error";
        }

        parseJsonForSetlist(setlist, response.getBody());

        log.info("SetlistId: " + setlist.getSetlistId());
        log.info("Name: " + setlist.getName());
        log.info("Artist: " + setlist.getArtist());
        log.info("Tracks: " + setlist.getSongNames());

        Optional<Playlist> playlist = playlistRepository.findById(setlist.getSetlistId());
        if (playlist.isPresent()) {
            log.info("Duplicate found. URL: " + playlist.get().getPlaylistUrl());
            return "redirect:" + playlist.get().getPlaylistUrl();
        } else {
            return "setlist_success";
        }
    }

    public String setlistIdParser(String setlistUrl) {
        //https://www.setlist.fm/setlist/metallica/2022/grant-park-chicago-il-23b230f3.html
        String[] tokens = setlistUrl.split("/");
        String tag = tokens[(tokens.length) - 1];

        //grant-park-chicago-il-23b230f3.html
        tokens = tag.split("-");
        tag = tokens[(tokens.length) - 1];

        //23b230f3.html
        int fileNameIndex = tag.indexOf(".");
        if (fileNameIndex > 0) {
            return tag.substring(0, fileNameIndex);
        } else if (fileNameIndex == -1) {
            return tag;
        } else {
            return "error handling should be better";
        }
    }

    public ResponseEntity<String> getSetlistItems(String setlistId) {
        String apiUrl = urlBuilder("/rest/1.0/setlist/", setlistId);
        HttpEntity<Void> entity = new HttpEntity<>(headersBuilder());

        return setlistFmApiRepository.get(apiUrl, entity, String.class);
    }

    public void parseJsonForSetlist(Setlist setlist, String setlistJson) {
        List<String> songNames = new ArrayList<>();
        String songName;

        try {
            JsonNode rootNode = objectMapper.readTree(setlistJson);
            setlist.setArtist(rootNode.path("artist").path("name").asText());
            JsonNode setsNode = rootNode.path("sets");
            JsonNode setNode = setsNode.path("set");
            JsonNode songNode;

            JsonNode tourNode = rootNode.path("tour");
            setlist.setName(tourNode.path("name").asText());

            for (JsonNode set : setNode) {
                songNode = set.path("song");
                for (JsonNode song : songNode) {
                    songName = song.path("name").asText();
                    if (songName.contains("/")) {
                        List<String> multipleSongs = List.of(songName.split("/"));
                        songNames.addAll(multipleSongs);
                    } else {
                        songNames.add(songName);
                    }
                }
            }
            setlist.setSongNames(songNames);
        }
        catch (JsonParseException e) {
            e.printStackTrace();
        }
        catch (JsonMappingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpHeaders headersBuilder() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", setlistFmConfig.getToken());

        return headers;
    }

    public String urlBuilder(String endPoint, String setlistId) {
        //TODO Proper URL encoding and building
        return setlistFmConfig.getApiUrl() + endPoint + setlistId;
    }
}
