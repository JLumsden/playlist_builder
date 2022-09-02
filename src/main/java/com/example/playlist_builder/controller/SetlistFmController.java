package com.example.playlist_builder.controller;

import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.service.SetlistFmService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@AllArgsConstructor
@Controller
public class SetlistFmController {

    SetlistFmService setlistFmService;

    @RequestMapping(value = "/setlist", method = RequestMethod.POST)
    public String postSetlistUrl(String setlistUrl) {

        log.info(setlistUrl);
        String setlistId = setlistFmService.setlistIdParser(setlistUrl);
        if (setlistId.equals("Invalid")) {
            return "error";
        }

        ResponseEntity<String> response = setlistFmService.getSetlistItems(setlistId);
        if (response.getStatusCodeValue() != 200) {
            return "error";
        }

        Setlist setlist = setlistFmService.parseJson(response.getBody());
        //log.info(songNames.toString());

        return "setlist_success";
    }

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String getHome() {
        return "index";
    }
}

//api key
//RYHtJtNU_uUkmWT7Nr9b4yMIgb_PL9jjnzB3
