package com.example.playlist_builder.authentication;

import com.example.playlist_builder.repository.SpotifyApiRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@AllArgsConstructor
public class SpotifyAuthService {

    private final SpotifyAuthConfig spotifyAuthConfig;
    private final SpotifyApiRepository spotifyApiRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAuthDataDelegator(AuthData authData, String authCode) {
        ResponseEntity<String> response = getAccessToken(authCode);
        if(response.getStatusCodeValue() != 200) {
            return "error";
        }
        return parseJsonForAuthData(response.getBody());
    }

    public String getAuthUrl() {
        return "https://accounts.spotify.com/en/authorize"
                + "?client_id=" + spotifyAuthConfig.getClientId()
                + "&response_type=code"
                + "&redirect_uri=" + spotifyAuthConfig.getRedirectUrl()
                //TODO implement state for CSRF protection
                //https://datatracker.ietf.org/doc/html/rfc6749#section-4.1
                + "&scope=" + spotifyAuthConfig.getAuthScopes();
    }

    public ResponseEntity<String> getAccessToken(String code) {
        MultiValueMap<String, String> payload = createAuthPayload(code);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(payload, createHeaders());

        return spotifyApiRepository.post(spotifyAuthConfig.getAuthUrl(), request, String.class);
    }

//    public String createAuthPayload(AuthData authData, String code) {
//        AuthPayloadDto authPayloadDto = new AuthPayloadDto(
//                spotifyAuthConfig.getClientId(),
//                "authorization_code",
//                code,
//                spotifyAuthConfig.getRedirectUrl(),
//                authData.getCode_verifier()
//        );
//
//        try {
//            return objectMapper.writeValueAsString(authPayloadDto);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public MultiValueMap<String, String> createAuthPayload(String authCode) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", authCode);
        map.add("redirect_uri", spotifyAuthConfig.getRedirectUrl());

        return map;
    }

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(spotifyAuthConfig.getClientId(), spotifyAuthConfig.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return headers;
    }

    public AuthData parseJsonForAuthData(String response) throws JsonProcessingException {
        AuthData authData = objectMapper.readValue(response, AuthData.class);
        //Should utilize SpotifyAuthConfig better here
        return authData;
    }
}
