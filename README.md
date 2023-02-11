# playlist_builder  
## Takes the tedium out of preparing for a concert  
### The What  
Playlist Builder is a Java/Spring Boot application that uses the Setlist.fm and Spotify APIs to build playlists from a given setlist.  
### The How  
I heavily indexed into the Java/Spring Boot environment to make use of the suite of very helpful tools. The project involves a lot of json, and thus Jackson and its ease of use was much appreciated. Spring Web and the Thymeleaf library were also a huge help in handling HTTP requests and returning dynamic html templates. The Setlist.fm API was simple enough to use and was a straight forward json delivery service. Spotify's API was a pain at times, but their developer console provided a nice playground to figure out queries and responses. Authentication to the Spotify API was done with a PKCE authorization flow with OAuth2.0.  
### The Why  
While growing up, my father and I always had a copy of a band's setlist to get excited and prep for an upcoming concert. I decided to lean into that idea by using some APIs to bring it to the modern age. Instead of filtering through a discography and burning a CD, it's done with a copy/pasted link and a couple button clicks.  

### To-Dos  
- [ ] Browser extension UI. Ideally a single button after sign in
- [ ] Implement accounts
- [ ] Implement Authorization Code flow instead of PKCE  
- [x] Add a database to store playlist links to cutdown on duplicate playlists.  
- [x] Handle track choosing from the search query better. Probably give the user more choice with this in choosing versions of songs.  
- [ ] Improve error handling.  
- [ ] Write tests.  
- [ ] Implement Spring Security and some input sanitizing with it. Don't want to send SQL injection attacks to either API provider.
