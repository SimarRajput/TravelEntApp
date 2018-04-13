package simar.travelentapp.HelperClasses;
import java.util.Date;

/**
 * Created by simar on 4/8/18.
 */

public class Reviews {
    private String revProfilePic;
    private String revName;
    private Date revDate;
    private String revReview;
    private String revUrl;
    private float revRating;

    //Default constructor
    public Reviews(){
        return;
    }

    //Copy constructor
    public Reviews(String revProfilePic, String revName, Date revDate, String revReview,String revUrl, float revRating){
        this.revProfilePic = revProfilePic;
        this.revName = revName;
        this.revDate = revDate;
        this.revReview = revReview;
        this.revRating = revRating;
        this.revUrl = revUrl;
    }

    public String getRevProfilePic() {
        return this.revProfilePic;
    }

    public String getRevName() {
        return this.revName;
    }

    public Date getRevDate() {
        return this.revDate;
    }

    public String getRevReview() {return this.revReview;}

    public String getRevUrl() {return this.revUrl;}

    public float getRevRating() {return this.revRating;}
}
