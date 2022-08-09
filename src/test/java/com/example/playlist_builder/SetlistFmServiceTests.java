package com.example.playlist_builder;

import com.example.playlist_builder.service.SetlistFmService;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
public class SetlistFmServiceTests {

    @Autowired
    SetlistFmService setlistFmService;

    @Test
    public void setlistIdParserTest() {
        String setlistUrl = "https://www.setlist.fm/setlist/metallica/2022/grant-park-chicago-il-23b230f3.html";
        String id = setlistFmService.setlistIdParser(setlistUrl);
        assertThat(id).isEqualTo("23b230f3");
    }

    @Test
    public void getSetlistItemsTest() {
        ResponseEntity<String> response = setlistFmService.getSetlistItems("23b230f3");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }
}
