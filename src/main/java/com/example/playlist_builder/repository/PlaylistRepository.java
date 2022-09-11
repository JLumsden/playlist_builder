package com.example.playlist_builder.repository;

import com.example.playlist_builder.data.Playlist;
import org.springframework.data.repository.CrudRepository;

public interface PlaylistRepository extends CrudRepository<Playlist, String> {
}
