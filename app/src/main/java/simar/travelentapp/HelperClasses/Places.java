package simar.travelentapp.HelperClasses;

/**
 * Created by simar on 4/8/18.
 */

public class Places {
    private String placeID;
    private String placeName;
    private String placeLocation;
    private String placeIcon;

    // Default Constructor
    public Places() {
        return;
    }

    public Places(String placeID, String placeName, String placeLocation, String placeIcon) {
        this.placeID = placeID;
        this.placeName = placeName;
        this.placeLocation = placeLocation;
        this.placeIcon = placeIcon;
    }

    public String getPlaceID() {
        return this.placeID;
    }

    public String getPlaceName() {
        return this.placeName;
    }

    public String getPlaceLocation() {

        return this.placeLocation;
    }

    public String getPlaceIcon() {

        return this.placeIcon;
    }
}
