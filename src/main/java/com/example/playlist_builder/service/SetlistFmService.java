package com.example.playlist_builder.service;

import com.example.playlist_builder.config.SetlistFmConfig;
import com.example.playlist_builder.data.SetlistDto;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
@Slf4j
public class SetlistFmService {
    private final RestTemplate restTemplate = new RestTemplate();
    SetlistFmConfig setlistFmConfig;
    ObjectMapper objectMapper;

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
            return "shit";
        }
    }

    public ResponseEntity<String> getSetlistItems(String setlistId) {
        String apiUrl = "https://api.setlist.fm/rest/1.0/setlist/" + setlistId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", setlistFmConfig.getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
    }

    public SetlistDto parseJson(String setlist) {
        List<String> songNames = new ArrayList<>();
        SetlistDto setlistDto = new SetlistDto();

        try {
            JsonNode rootNode = objectMapper.readTree(setlist);
            setlistDto.setArtist(rootNode.path("artist").path("name").asText());
            log.info("artist = " + setlistDto.getArtist());
            JsonNode setsNode = rootNode.path("sets");
            log.info("setsNode = " + setsNode);
            JsonNode setNode = setsNode.path("set");
            log.info("setNode = " + setNode);
            JsonNode songNode;
            for (JsonNode set : setNode) {
                log.info("set = " + set);
                songNode = set.path("song");
                for (JsonNode song : songNode) {
                    songNames.add(song.path("name").asText());
                }
            }
            setlistDto.setSongNames(songNames);
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
        return setlistDto;
    }
}
