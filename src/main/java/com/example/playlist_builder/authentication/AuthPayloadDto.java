package com.example.playlist_builder.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthPayloadDto {
    private String client_id;
    private String grant_type;
    private String code;
    private String redirect_uri;
    private String code_verifier;
}
