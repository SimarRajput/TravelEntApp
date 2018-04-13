package simar.travelentapp.Tabs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import simar.travelentapp.Adapters.AdapterPhotos;
import simar.travelentapp.R;

public class PhotosTab extends Fragment {

    protected GeoDataClient _geoDataClient;
    private AdapterPhotos _adapterPhotos;
    ArrayList<Bitmap> _photosList = new ArrayList<>();
    String _placeID = "";
    View _rootView;
    TextView _emptyView;
    RecyclerView _recPhotos;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.photos_tab, container, false);

        _emptyView = (TextView) _rootView.findViewById(R.id.emptyView);

        Bundle bundleDetails = getArguments();
        _placeID = bundleDetails.getString("PhotosPlaceID");

        //Initialize Recycler View
        _recPhotos = (RecyclerView) _rootView.findViewById(R.id.recPhotos);
        _recPhotos.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapterPhotos = new AdapterPhotos(getActivity());
        _recPhotos.setAdapter(_adapterPhotos);

        _geoDataClient = Places.getGeoDataClient(this.getContext(), null);
        getPhotos();
        return _rootView;
    }

    // Request photos and metadata for the specified place.
    private void getPhotos() {
        final ArrayList<PlacePhotoMetadata> photosDataList = new ArrayList<>();
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = _geoDataClient.getPlacePhotos(_placeID);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                int j = 0;
                for(PlacePhotoMetadata photoMetadata : photoMetadataBuffer){
                    photosDataList.add(photoMetadataBuffer.get(j).freeze());
                    j++;
                }
                photoMetadataBuffer.release();
                if(photosDataList.size() > 0){
                    for (int i = 0; i < photosDataList.size(); i++) {
                        getPhoto(photosDataList.get(i));
                    }

                    _recPhotos.setVisibility(View.VISIBLE);
                    _emptyView.setVisibility(View.GONE);
                } else {
                    _recPhotos.setVisibility(View.GONE);
                    _emptyView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getPhoto(PlacePhotoMetadata photoMetadata){
        Task<PlacePhotoResponse> photoResponse = _geoDataClient.getPhoto(photoMetadata);
        Bitmap photoBitmap = null;
        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {

            @Override
            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                PlacePhotoResponse photo = task.getResult();
                Bitmap photoBitmap = photo.getBitmap();
                _photosList.add(photoBitmap);
                _adapterPhotos.setPhotosList(_photosList);
            }
        });
    }
}