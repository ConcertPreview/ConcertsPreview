package com.example.jinjinz.concertprev.models;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Parcel
public class Concert {


    //root url: https://app.ticketmaster.com/discovery/v2/
    // events url: https://app.ticketmaster.com/discovery/v2/events

    private long dbId = -1L;
    private String backdropImage;
    private String headliner;
    private String venue;
    private String artistsString;
    private String eventName;
    private String eventTime;
    private String eventDate;
    private String city;
    private String stateCode;
    private String countryCode;
    private String eventUrl;
    private ArrayList<String> artists;
    private boolean liked;


    public void setDbId(long dbId) {
        this.dbId = dbId;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public void setBackdropImage(String backdropImage) {
        this.backdropImage = backdropImage;
    }
    public void setArtists(ArrayList<String> artists) {
        this.artists = artists;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }
    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public void setArtistsString(String artistsString) {
        this.artistsString = artistsString;
    }
    public void setLiked(boolean liked) { this.liked = liked; }

    public String getArtistsString() {
        return artistsString;
    }
    public long getDbId() {
        return dbId;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public String getStateCode() {
        return stateCode;
    }
    public String getCity() {
        return city;
    }
    public String getEventDate() {
        return eventDate;
    }
    public String getEventTime() {
        return eventTime;
    }
    public String getEventName() {
        return eventName;
    }
    public String getVenue() {
        return venue;
    }
    public String getEventUrl() {
        return eventUrl;
    }
    public ArrayList<String> getArtists() {
        return artists;
    }
    public String getBackdropImage() {
        return backdropImage;
    }
    public boolean isLiked() { return liked;}



    public Concert() {

    }

    /**
     * Builds and returns an ArrayList of Concerts from the supplied JSONArray from the Ticketmaster API
     * @param jsonArray the events array from the ticketmaster client call
     * @return arraylist of concerts built from the client call
     * */
    public static ArrayList<Concert> concertsFromJsonArray(JSONArray jsonArray) { // looking for the "events" array --> { _embedded: { events: [ {0, 1, 2, ..} ] } } within the larger "_embedded" array and the largest object that you get from the client response
        ArrayList<Concert> concert = new ArrayList<>();
        // iterate
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject eventObj = null;
            try {
                eventObj = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject artistObj = null;
            try {
                if (eventObj != null) {
                    artistObj = eventObj.getJSONObject("_embedded");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!artistObj.has("attractions")) { // if the event object does not have an artist array, continue
            } else { // if it contains the attractions array (it is a concert and has at least one artist)
                concert.add(Concert.fromJsonObject(eventObj)); // else, add the object
                Log.d("populateFragment", "concertarray");
            }
        }

        return concert;
    }

    /**
     * Builds and returns an ArrayList of artist names with the supplied artist(attractions) JSONArray from the Ticketmaster API
     * @param attractions the json array of the artists of one concert
     * @return arraylist of artist names
     * */
    private static ArrayList<String> artistsFromJsonArray(JSONArray attractions) { // the attractions array: { _embedded:{ events:[ { ..., _embedded:{ venues:[...], attractions:[ list of at least one artist ] } } ] } }
        ArrayList<String> array = new ArrayList<>();
        // iterate
        for (int i = 0; i < attractions.length(); i++) {
            try {
                array.add(attractions.getJSONObject(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    // get a wide image that's large enough to be pretty
    /**
     * Finds and returns a backdrop image for a concert with a ratio made to fit cleanly into the concert ImageView and a size large enough to appear crisp onscreen
     * @param event the event json object representing one concert
     * @return the url of the desired image
     * */
    private static String ratioImg(JSONObject event) { //takes an event obj --> { events:[ {0}, {1}, ...]} needs: 0:{images:[ {0}, {1}, ... ]}
        // start with event obj

        JSONArray images = null;
        try {
            images = event.getJSONArray("images"); // get the json array of images
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (images != null) { // if the images jsonarray exists
            for (int i = 0; i < images.length(); i++) { // step through array
                try {                                                               // get ratio of the image obj
                    if(images.getJSONObject(i).getString("ratio").equals("16_9")) { // if the ratio is 16_9
                        if(images.getJSONObject(i).getInt("width") >= 400) {
                            return images.getJSONObject(i).getString("url"); // return the url of that img obj
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try { // if none are 16_9, go get the first one
                return event.getJSONArray("images").getJSONObject(0).getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // if no image array exists, use stock img
        return "http://www.wallpapersxl.com/wallpapers/1920x1080/concerts/1018922/concerts-skate-music-concert-noise-jpg-with-resolution-1018922.jpg";

    }

    /**
     * Builds and returns a concert from the event JSONObject retrieved from the Ticketmaster API
     * */
    public static Concert fromJsonObject(JSONObject event){ // will give the concert each obj from the "events" json array (each index) // then will form each obj from the fromJsonArray method
        Concert concert = new Concert();
        // extract the values from the json, store them
        try {
            concert.backdropImage = ratioImg(event);
            concert.eventName = event.getString("name");
            // because I love Chance
            if(concert.getEventName().contains("Chance The Rapper")) {
                concert.backdropImage = "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-372461.png";
            }
            concert.artists = artistsFromJsonArray(event.getJSONObject("_embedded").getJSONArray("attractions"));
            concert.artistsString = android.text.TextUtils.join(", ", concert.artists);
            if (concert.artists.size() != 0) {
                concert.headliner = concert.artists.get(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            concert.countryCode = event.getJSONObject("_embedded").getJSONArray("venues").getJSONObject(0)
                    .getJSONObject("country").getString("countryCode"); // if country != "US", use country code in place of stateCode
            concert.city = event.getJSONObject("_embedded").getJSONArray("venues")
                    .getJSONObject(0).getJSONObject("city").getString("name");
            concert.venue = event.getJSONObject("_embedded").getJSONArray("venues")
                    .getJSONObject(0).getString("name");
            String state = event.getJSONObject("_embedded").getJSONArray("venues")
                    .getJSONObject(0).optJSONObject("state").optString("stateCode");
            if (state.equals("")) {
                concert.stateCode = null;
            } else {
                concert.stateCode = state;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            concert.eventDate = formatDate(event.getJSONObject("dates").getJSONObject("start").getString("localDate"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            concert.eventTime = event.getJSONObject("dates").getJSONObject("start").getString("localTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            concert.eventUrl = event.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return concert;
    }

    /**
     * Turns the list of artists into an array list of artists for use by the Spotify API
     * @param artistList list of artists joined by commas
     * @return array list of artists
     */
    public ArrayList<String> artistListToArray(String artistList) {
        ArrayList<String> artists = new ArrayList<>();
        String[] artistArray = TextUtils.split(artistList, ", ");
        for(int i = 0; i < artistArray.length; i++) {
            artists.add(artistArray[i]);
        }
        return artists;
    }

    /** Formats the date from the Ticketmaster API into a clean, easily readable format */
    public static String formatDate(String originalDate){
        SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try {
            newDate = sdFormatter.parse(originalDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        sdFormatter = new SimpleDateFormat("MMM dd, yyyy");
        sdFormatter = new SimpleDateFormat("MMM dd, yyyy");
        String date = sdFormatter.format(newDate);
        return date;
    }

}
