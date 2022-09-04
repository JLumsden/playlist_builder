package com.example.playlist_builder.controller;

import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.service.SetlistFmService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@SessionAttributes("setlist")
@Slf4j
@AllArgsConstructor
@Controller
public class SetlistFmController {

    SetlistFmService setlistFmService;

    @RequestMapping(value = "/setlist", method = RequestMethod.POST)
    public String postSetlistUrl(@ModelAttribute("setlist") Setlist setlist, String setlistUrl) {
        return setlistFmService.postPlaylistUrlDelegator(setlist, setlistUrl);
    }
    @RequestMapping(value="/", method = RequestMethod.GET)
    public String getHome() {
        return "index";
    }
    @RequestMapping(value="/error", method = RequestMethod.GET)
    public String getError() {
        return "error";
    }
    @ModelAttribute("setlist")
    public Setlist getSetlist() {
        return new Setlist();
    }
}
