package com.example.playlist_builder.controller;

import com.example.playlist_builder.data.AuthData;
import com.example.playlist_builder.data.Setlist;
import com.example.playlist_builder.service.SetlistFmService;
import com.example.playlist_builder.service.SpotifyApiService;
import com.example.playlist_builder.service.SpotifyAuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@SessionAttributes({"setlist","authData"})
@Slf4j
@AllArgsConstructor
@Controller
public class SetlistFmController {

    private final SetlistFmService setlistFmService;
    private final SpotifyApiService spotifyApiService;
    private final SpotifyAuthService spotifyAuthService;

    @RequestMapping(value = "/setlist", method = RequestMethod.POST)
    public String postSetlistUrl(@ModelAttribute("setlist") Setlist setlist, String setlistUrl) {
        return setlistFmService.postPlaylistUrlDelegator(setlist, setlistUrl);
    }

    @RequestMapping(value = "/playlist", method = RequestMethod.GET)
    public String buildPlaylist(@ModelAttribute("setlist") Setlist setlist, @ModelAttribute("authData") AuthData authData) {
        return spotifyApiService.buildPlaylistDelegator(setlist, authData.getAccess_token());
    }

    @RequestMapping(value = "/auth", method = RequestMethod.GET)
    public ModelAndView performAuthentication(@ModelAttribute("authData") AuthData authData) {
        spotifyAuthService.performAuthenticationDelegator(authData);
        return new ModelAndView("redirect:" + spotifyAuthService.getAuthUrl(authData));
    }

    @RequestMapping(value = "/authenticated", method = RequestMethod.GET)
    public String authenticated(@ModelAttribute("authData") AuthData authData, @RequestParam(value = "code") final String authCode) {
        return spotifyAuthService.getAccessTokenDelegator(authData, authCode);
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
    @ModelAttribute("authData")
    public AuthData getAuthData() {
        return new AuthData();
    }
}
