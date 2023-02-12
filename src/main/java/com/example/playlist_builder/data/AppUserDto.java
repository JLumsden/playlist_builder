package com.example.playlist_builder.data;

import com.example.playlist_builder.authentication.AuthData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserDto {
    private String userEmail;
    private AuthData userAuthData;
}
