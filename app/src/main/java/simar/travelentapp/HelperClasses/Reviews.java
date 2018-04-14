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

    public Reviews(Reviews copyReviews){
        this.revProfilePic = new String(copyReviews.revProfilePic);
        this.revName = new String(copyReviews.revName);
        this.revDate = new Date(String.valueOf(copyReviews.revDate));
        this.revReview = new String(copyReviews.revReview);
        this.revRating = new Float(copyReviews.revRating);
        this.revUrl = new String(copyReviews.revUrl);
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
