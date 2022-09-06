package com.example.playlist_builder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
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

    public <T> String dtoToJson(T dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    //Parses nested search result json
    public String parseSearchResultForUri(String json, String artist) {
        try {
            JsonNode artistsNode, tracksNode, itemsNode;

            //Dig down nested json
            JsonNode rootNode = objectMapper.readTree(json);
            tracksNode = rootNode.path("tracks");
            itemsNode = tracksNode.path("items");

            //At the tracks list
            for (JsonNode trackNode : itemsNode) {
                //TODO THIS IS SO BAD. DO BETTER
                //Iterate through artists list
                artistsNode = trackNode.path("artists");
                for (JsonNode artistNode : artistsNode) {
                    //Ensures song chosen is by artist
                    //Json is ordered alphabetically by artist, not relevancy
                    //Will change in the future to account for popularity metric
                    //Probably return top 3-5 and let user choose
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
}
