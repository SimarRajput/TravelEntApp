package simar.travelentapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsTab extends Fragment {
    LatLng _placeLatLang;
    View _rootView;
    MapView _MapView;
    private GoogleMap _googleMap;
    Spinner _spinnerMapMode;
    double _lat = 0;
    double _lon = 0;
    protected GeoDataClient _geoDataClient;
    private AdapterAutocomplete _autoCompAdapter;
    boolean _locationPerGranted = false;
    private static final int _autoCompleteReqCode = 1;
    AutoCompleteTextView _txtFromLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.maps_tab, container, false);

        _geoDataClient = com.google.android.gms.location.places.Places.getGeoDataClient(getActivity(), null);

        Bundle bundleDetails = getArguments();
        String[] placeLatLangString = bundleDetails.getString("LatLng").split(",");
        double latitude = Double.parseDouble(placeLatLangString[0]);
        double longitude = Double.parseDouble(placeLatLangString[1]);
        _placeLatLang = new LatLng(latitude,longitude);

        _MapView = (MapView) _rootView.findViewById(R.id.mapView);
        _MapView.onCreate(savedInstanceState);
        _MapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        _MapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                _googleMap = mMap;
                _googleMap.addMarker(new MarkerOptions().position(_placeLatLang));

                CameraPosition cameraPosition = new CameraPosition.Builder().target(_placeLatLang).zoom(12).build();
                _googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        // Spinner Initialization
        _spinnerMapMode = (Spinner) _rootView.findViewById(R.id.spinnerMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.getActivity(), R.array.map_mode_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerMapMode.setAdapter(adapter);

        _txtFromLocation = (AutoCompleteTextView) _rootView.findViewById(R.id.txtFromLocation);
        _autoCompAdapter = new AdapterAutocomplete(getActivity(), _geoDataClient, null, null);
        _txtFromLocation.setAdapter(_autoCompAdapter);
        _txtFromLocation.setOnItemClickListener(mAutocompleteClickListener);

        return _rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _MapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        _MapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _MapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _MapView.onLowMemory();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = _autoCompAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Task<PlaceBufferResponse> placeResult = _geoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                LatLng latLng = place.getLatLng();

                _lat = latLng.latitude;
                _lon = latLng.longitude;

                places.release();
            } catch (RuntimeRemoteException e) {
                return;
            }
        }
    };
}


