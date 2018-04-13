package simar.travelentapp.HelperClasses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import simar.travelentapp.R;

/**
 * Created by simar on 4/13/18.
 */

public class DataParser {

    //region Public Methods

    // Default Constructor
    public DataParser(){
        return;
    }

    // Parse Direction Data
    public String[] parseJSONDirections(JSONObject directions) {
        JSONArray jsonArray = null;
        try {
            jsonArray = directions.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPaths(jsonArray);
    }

    public ArrayList<Reviews> parseJSONReviews(JSONArray reviews) {
        ArrayList<Reviews> reviewsList = new ArrayList<>();

        for (int i = 0; i < reviews.length(); i++) {
            JSONObject jsonPlace = null;
            try {
                jsonPlace = reviews.getJSONObject(i);

                String revUrl = jsonPlace.getString("author_url");
                String revProfilePic = jsonPlace.getString("profile_photo_url");
                String revName = jsonPlace.getString("author_name");
                String revReview = jsonPlace.getString("text");
                float revRating = Float.parseFloat(jsonPlace.getString("rating"));

                long unixSeconds = Long.parseLong(jsonPlace.getString("time"));
                Date date = new java.util.Date(unixSeconds * 1000L);
                SimpleDateFormat simpleDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDate.setTimeZone(java.util.TimeZone.getTimeZone("GMT-8"));
                Date revDate = null;
                try {
                    revDate = simpleDate.parse(simpleDate.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Reviews review = new Reviews(revProfilePic, revName, revDate, revReview, revUrl, revRating);
                reviewsList.add(review);


            } catch (JSONException e) {
                //return empty reviews List
            }
        }
        return reviewsList;
    }

    public ArrayList<Reviews> parseJSONReviewsYelp(JSONArray yelpReviews) {
        ArrayList<Reviews> reviewsList = new ArrayList<>();
        for (int i = 0; i < yelpReviews.length(); i++) {
            JSONObject jsonPlace = null;
            try {
                jsonPlace = yelpReviews.getJSONObject(i);
                String revUrl = jsonPlace.getString("url");
                String revProfilePic = jsonPlace.getJSONObject("user").getString("image_url");
                String revName = jsonPlace.getJSONObject("user").getString("name");
                String revReview = jsonPlace.getString("text");
                float revRating = Float.parseFloat(jsonPlace.getString("rating"));
                Date revDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonPlace.getString("time_created"));

                Reviews review = new Reviews(revProfilePic, revName, revDate, revReview, revUrl, revRating);
                reviewsList.add(review);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return reviewsList;
    }

    public ArrayList<Places> parseJSONPlaces(JSONObject searchResults) {
        ArrayList<Places> placesList = new ArrayList<>();
        JSONArray jsonPlaces = null;

        try {
            jsonPlaces = searchResults.getJSONArray("results");


            for (int i = 0; i < jsonPlaces.length(); i++) {
                JSONObject jsonPlace = jsonPlaces.getJSONObject(i);

                String placeID = jsonPlace.getString("place_id");
                String placeName = jsonPlace.getString("name");
                String placeLocation = jsonPlace.getString("vicinity");
                String placeIcon = jsonPlace.getString("icon");

                Places place = new Places(placeID, placeName, placeLocation, placeIcon);
                placesList.add(place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return placesList;
    }

    //endregion

    //region Private Methods

    //Method to Parse Directions Data
    private String[] getPaths(JSONArray googleStepsJson) {
        int stepsCount = googleStepsJson.length();
        String[] polylines = new String[stepsCount];

        for (int i = 0; i < stepsCount; i++) {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return polylines;
    }
    //Method to Parse Directions Data
    private String getPath(JSONObject googlePathJson) {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }
    //endregion
}
