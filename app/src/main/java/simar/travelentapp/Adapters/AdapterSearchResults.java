package simar.travelentapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import simar.travelentapp.HelperClasses.DatabaseHelper;
import simar.travelentapp.HelperClasses.Places;
import simar.travelentapp.R;

/**
 * Created by simar on 4/8/18.
 */

public class AdapterSearchResults extends RecyclerView.Adapter<AdapterSearchResults.ViewHolderSearchResults> {
    private OnItemClickListener _itemClickListner;

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onFavoriteButtonClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListner) {
        this._itemClickListner = itemClickListner;
    }

    private LayoutInflater _layoutInflater;
    private ArrayList<Places> _placesList = new ArrayList<>();
    private ArrayList<String> _favoritePlaceIdList = new ArrayList<>();
    private DatabaseHelper _databaseHelper;

    // Constructor
    public AdapterSearchResults(Context context) {
        //Initialize Database
        _databaseHelper = new DatabaseHelper(context);

        //Fill favorites array list
        _favoritePlaceIdList = _databaseHelper.getFavoritePlaceIdList();

        _layoutInflater = LayoutInflater.from(context);
    }

    // Set Places List
    public void setPlacesList(ArrayList<Places> placeList) {
        //Fill favorites array list
        _favoritePlaceIdList = _databaseHelper.getFavoritePlaceIdList();

        this._placesList = placeList;
        notifyItemRangeChanged(0, placeList.size());
    }

    @Override
    public ViewHolderSearchResults onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = _layoutInflater.inflate(R.layout.search_results_card, parent, false);

        ViewHolderSearchResults viewHolder = new ViewHolderSearchResults(view, _itemClickListner);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolderSearchResults holder, int position) {
        final Places place = _placesList.get(position);

        Picasso.get().load(place.getPlaceIcon()).into(holder.imgCatType);
        holder.txtName.setText(place.getPlaceName());
        holder.txtLocation.setText(place.getPlaceLocation());
        holder.btnFav.setTag(place.getPlaceID());

        //Check if place is already in favorites then make button red.
        if(checkIfPlaceInFavorites(place.getPlaceID())){
            holder.btnFav.setBackgroundResource(R.drawable.heart_red);
        } else{
            holder.btnFav.setBackgroundResource(R.drawable.heart_outline_black);
        }
    }

    @Override
    public int getItemCount() {
        return _placesList.size();
    }

    public class ViewHolderSearchResults extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private ImageView imgCatType;
        private TextView txtName;
        private TextView txtLocation;
        private Button btnFav;
        private WeakReference<OnItemClickListener> listenerRef;

        public ViewHolderSearchResults(final View view, OnItemClickListener listner) {
            super(view);
            imgCatType = (ImageView) view.findViewById(R.id.imgCatType);
            txtName = (TextView) view.findViewById(R.id.txtName);
            txtLocation = (TextView) view.findViewById(R.id.txtLocation);
            btnFav = (Button) view.findViewById(R.id.btnFav);

            listenerRef = new WeakReference<>(listner);

            btnFav.setOnClickListener(this);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (_itemClickListner != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            _itemClickListner.onItemClick(view, position);
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            listenerRef.get().onFavoriteButtonClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    private boolean checkIfPlaceInFavorites(String placeId){
        int index = _favoritePlaceIdList.indexOf(placeId);

        if(index < 0){
            return false;
        }else{
            return true;
        }
    }
}

