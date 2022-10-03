package com.example.playlist_builder.service;

import com.example.playlist_builder.comparator.TrackPopularityComparator;
import com.example.playlist_builder.data.Track;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class JsonService {
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public String parseForUrl(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode external_urls = rootNode.path("external_urls");
            return external_urls.path("spotify").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> String dtoToJson(T dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //Parses nested search result json
    public String parseSearchResultForUri(String json, String artist, String trackName) {
        try {
            JsonNode tracksNode, itemsNode;
            List<Track> trackList = new ArrayList<>();

            //Dig down nested json
            JsonNode rootNode = objectMapper.readTree(json);
            tracksNode = rootNode.path("tracks");
            itemsNode = tracksNode.path("items");

            //At the tracks list
            for (JsonNode trackNode : itemsNode) {
                populateTrackList(trackList, trackNode, artist, trackName);
            }
            return findMostPopularUri(trackList);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void populateTrackList(List<Track> trackList, JsonNode trackNode,
                                  String artist, String trackName) {
        if (filterTrack(trackNode, artist, trackName)) {
            trackList.add(createTrackFromJson(trackNode));
        }
    }

    public boolean filterTrack(JsonNode trackNode, String artist, String trackName) {
        if (!isTrackName(trackNode, trackName)) {
            return false;
        }
        if (!isArtist(trackNode, artist)) {
            return false;
        }
        return true;
    }

    public boolean isArtist(JsonNode trackNode, String artist) {
        JsonNode artistsNode = trackNode.path("artists");
        for (JsonNode artistNode : artistsNode) {
            //Ensures song chosen is by artist
            //Json is ordered alphabetically by artist, not relevancy
            //Will change in the future to account for popularity metric
            //Probably return top 3-5 and let user choose
            if (artistNode.path("name").asText().equals(artist)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTrackName(JsonNode trackNode, String trackName) {
        String brokenDownJsonEntry, brokenDownTrackName;

        //Converts to lower case and removes all spaces
        brokenDownJsonEntry = trackNode.path("name").asText().toLowerCase().replaceAll("\\s", "");
        brokenDownTrackName = trackName.toLowerCase().replaceAll("\\s", "");

        if (brokenDownJsonEntry.contains(brokenDownTrackName)) {
            return true;
        }
        return false;
    }

    public String findMostPopularUri(List<Track> trackList) {
        Collections.sort(trackList, new TrackPopularityComparator());
        if (trackList.isEmpty()) {
            //TODO
            return "error";
        }
        return trackList.get(0).getUri();
    }

    public Track createTrackFromJson(JsonNode trackNode) {
        return new Track(
                parseForPopularity(trackNode),
                parseForUri(trackNode)
        );
    }

    public int parseForPopularity(JsonNode node) {
        return node.path("popularity").asInt();
    }

    public String parseForUri(JsonNode node) {
        return node.path("uri").asText();
    }
}
