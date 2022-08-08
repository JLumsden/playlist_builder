package com.example.playlist_builder.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AuthData {
    private String code_verifier;
    private String code_challenge;
    private String access_token;
}
