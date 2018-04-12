package simar.travelentapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by simar on 4/8/18.
 */

public class AdapterReviews extends RecyclerView.Adapter<AdapterReviews.ViewHolderReviews> {

    private LayoutInflater _layoutInflater;
    ArrayList<Reviews> _reviewsList = new ArrayList<>();

    public AdapterReviews(Context context){
        _layoutInflater = LayoutInflater.from(context);
    }

    public void setReviewsList(ArrayList<Reviews> reviewsList){
        this._reviewsList = reviewsList;
        notifyItemRangeChanged(0, reviewsList.size());
    }

    @Override
    public ViewHolderReviews onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _layoutInflater.inflate(R.layout.review_card, parent, false);

        ViewHolderReviews viewHolder = new ViewHolderReviews(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolderReviews holder, int position) {
        Reviews review = _reviewsList.get(position);

        Picasso.get().load(review.getRevProfilePic()).into(holder.imagePerson);
        holder.txtPersonName.setText(review.getRevName());
        holder.ratingBarReview.setRating(review.getRevRating());
        holder.txtReview.setText(review.getRevReview());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String revDate = dateFormat.format(review.getRevDate());
        holder.txtDate.setText(revDate);
    }

    @Override
    public int getItemCount() {
        return _reviewsList.size();
    }

    static class ViewHolderReviews extends RecyclerView.ViewHolder{
        private ImageView imagePerson;
        private TextView txtPersonName;
        private RatingBar ratingBarReview;
        private TextView txtDate;
        private TextView txtReview;

        public ViewHolderReviews(View view) {
            super(view);
            imagePerson = (ImageView) view.findViewById(R.id.imagePerson);
            txtPersonName = (TextView) view.findViewById(R.id.txtPersonName);
            ratingBarReview = (RatingBar) view.findViewById(R.id.ratingBarReview);
            txtDate = (TextView) view.findViewById(R.id.txtDate);
            txtReview = (TextView) view.findViewById(R.id.txtReview);
        }
    }

}

