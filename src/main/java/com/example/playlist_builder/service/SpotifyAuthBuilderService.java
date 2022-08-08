package com.example.playlist_builder.service;

import com.example.playlist_builder.data.AuthData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class SpotifyAuthBuilderService {

    private final String clientId = "37a89037aa044be69a0022023a319d72";
    private final String redirectUrl = "http://localhost:8080/redirect";


    private final RestTemplate restTemplate = new RestTemplate();

    public String getAuthUrl(AuthData authData) {
        final String authScopes = "playlist-read-collaborative,playlist-modify-public,playlist-read-private,playlist-modify-private";

        return "https://accounts.spotify.com/en/authorize?client_id=" + this.clientId
                + "&response_type=code&redirect_uri=" + this.redirectUrl
                + "&code_challenge_method=S256&code_challenge=" + authData.getCode_challenge()
                + "&scope=" + authScopes
                + "&show_dialog=true";
    }

    public ResponseEntity getAccessToken(AuthData authData, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", this.clientId);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", this.redirectUrl);
        map.add("code_verifier", authData.getCode_verifier());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.exchange("https://accounts.spotify.com/api/token", HttpMethod.POST, request, String.class);

        return response;
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
}
