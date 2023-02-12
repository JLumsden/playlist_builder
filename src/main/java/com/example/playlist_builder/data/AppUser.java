package com.example.playlist_builder.data;

import com.example.playlist_builder.authentication.AuthData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class AppUser {
    @Id
    @GeneratedValue
    private UUID userId;
    private String userEmail;
    private String encodedPassword;
    private AuthData userAuthData;
}
