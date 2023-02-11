package com.example.playlist_builder.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AuthData {
    private String access_token;
    private String token_type;
    private String scope;
    private int expires_in;
    private String refresh_token;
}
