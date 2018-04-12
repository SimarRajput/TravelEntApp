package simar.travelentapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.location.places.GeoDataClient;

import java.util.ArrayList;

/**
 * Created by simar on 4/8/18.
 */

public class AdapterPhotos extends RecyclerView.Adapter<AdapterPhotos.ViewHolderPhotos> {

    private LayoutInflater layoutInflater;
    protected GeoDataClient _geoDataClient;
    ArrayList<Bitmap> _placesPhotoList = new ArrayList<>();

    public AdapterPhotos(Context context){
        layoutInflater = LayoutInflater.from(context);
    }

    public void setPhotosList(ArrayList<Bitmap> placePhotoList){
        this._placesPhotoList = placePhotoList;
        notifyItemRangeChanged(0, placePhotoList.size());
    }

    @Override
    public ViewHolderPhotos onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.photo_card, parent, false);
        ViewHolderPhotos viewHolder = new ViewHolderPhotos(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolderPhotos holder, int position) {
        Bitmap placePhoto = _placesPhotoList.get(position);
        holder.imgPlaces.invalidate();
        holder.imgPlaces.setImageBitmap(placePhoto);
    }

    @Override
    public int getItemCount() {
        return _placesPhotoList.size();
    }

    static class ViewHolderPhotos extends RecyclerView.ViewHolder{
        private ImageView imgPlaces;

        public ViewHolderPhotos(View view) {
            super(view);
            imgPlaces = (ImageView) view.findViewById(R.id.imgPlaces);
        }
    }
}

