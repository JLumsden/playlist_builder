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

    public void performAuthenticationDelegator(AuthData authData) {
        createCodeVerifier(authData);
        createCodeChallenge(authData);
    }

    public String getAccessTokenDelegator(AuthData authData, String authCode) {
        ResponseEntity<String> response = getAccessToken(authData, authCode);
        if(response.getStatusCodeValue() != 200) {
            return "error";
        }
        return parseJsonForAccessToken(response.getBody(), authData);
    }

    public String getAuthUrl(AuthData authData) {
        return "https://accounts.spotify.com/en/authorize?client_id=" + spotifyAuthConfig.getClientId()
                + "&response_type=code&redirect_uri=" + spotifyAuthConfig.getRedirectUrl()
                + "&code_challenge_method=S256&code_challenge=" + authData.getCode_challenge()
                + "&scope=" + spotifyAuthConfig.getAuthScopes()
                + "&show_dialog=true";
    }

    public ResponseEntity<String> getAccessToken(AuthData authData, String code) {
        HttpEntity<String> request = new HttpEntity<>(createAuthPayload(authData, code), createHeaders());

        return spotifyApiRepository.post(spotifyAuthConfig.getAuthUrl(), request, String.class);
    }

    public void createCodeVerifier(AuthData authData) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        authData.setCode_verifier(Base64.getUrlEncoder()
                .withoutPadding().encodeToString(codeVerifier));
    }

    public void createCodeChallenge(AuthData authData) {
        byte[] digest = null;
        try {
            byte[] bytes = authData.getCode_verifier().getBytes("US-ASCII");
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            digest = messageDigest.digest();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException exception) {
            log.error("Unable to generate code challenge {}", exception);
        }
        authData.setCode_challenge(Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
    }

    public String createAuthPayload(AuthData authData, String code) {
        AuthPayloadDto authPayloadDto = new AuthPayloadDto(
                spotifyAuthConfig.getClientId(),
                "authorization_code",
                code,
                spotifyAuthConfig.getRedirectUrl(),
                authData.getCode_verifier()
        );

        try {
            return objectMapper.writeValueAsString(authPayloadDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return headers;
    }

    public String parseJsonForAccessToken(String response, AuthData authData) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            authData.setAccess_token(rootNode.path("access_token").asText());
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Should utilize SpotifyAuthConfig better here
        return "authenticated";
    }
}
