package simar.travelentapp.Tabs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import simar.travelentapp.Adapters.AdapterAutocomplete;
import simar.travelentapp.Controllers.AppController;
import simar.travelentapp.HelperClasses.DataParser;
import simar.travelentapp.R;

public class MapsTab extends Fragment {
    //region Variables
    private LatLng _placeLatLang;
    private AdapterAutocomplete _autoCompAdapter;
    private View _rootView;
    private MapView _MapView;
    private GoogleMap _googleMap;
    private Spinner _spinnerMapMode;
    private AutoCompleteTextView _txtFromLocation;
    private DataParser _dataParser;

    private double _latOrigin = 0;
    private double _lonOrigin = 0;
    private static final int _autoCompleteReqCode = 1;

    protected GeoDataClient _geoDataClient;
    //endregion

    //region Override Methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Initialize global variables
        _rootView = inflater.inflate(R.layout.maps_tab, container, false);
        _dataParser = new DataParser();
        _geoDataClient = com.google.android.gms.location.places.Places.getGeoDataClient(getActivity(), null);

        //Get intent data
        Bundle bundleDetails = getArguments();
        String[] placeLatLangString = bundleDetails.getString("LatLng").split(",");
        double latitude = Double.parseDouble(placeLatLangString[0]);
        double longitude = Double.parseDouble(placeLatLangString[1]);
        _placeLatLang = new LatLng(latitude, longitude);

        //Initialize maps
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

                CameraPosition cameraPosition = new CameraPosition.Builder().target(_placeLatLang).zoom(14).build();
                _googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });

        // Spinner Initialization
        _spinnerMapMode = (Spinner) _rootView.findViewById(R.id.spinnerMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.getActivity(), R.array.map_mode_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerMapMode.setAdapter(adapter);
        _spinnerMapMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (_latOrigin != 0 && _lonOrigin != 0) {
                    getDirections(_spinnerMapMode.getItemAtPosition(position).toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        // Initialize form
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
    //endregion

    //region Listners
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

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback = new OnCompleteListener<PlaceBufferResponse>() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                LatLng latLng = place.getLatLng();

                _latOrigin = latLng.latitude;
                _lonOrigin = latLng.longitude;

                places.release();

                getDirections(_spinnerMapMode.getSelectedItem().toString());
            } catch (RuntimeRemoteException e) {
                return;
            }
        }
    };
    //endregion

    //region Private Methods
    private void getDirections(String mode) {
        String url = "";
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin=" + _latOrigin + "," + _lonOrigin);
        googleDirectionsUrl.append("&destination=" + _placeLatLang.latitude + "," + _placeLatLang.longitude);
        googleDirectionsUrl.append("&mode=" + mode.toLowerCase());
        googleDirectionsUrl.append("&key=" + "AIzaSyCezipVJkYSdRmEtwdg37OEgW7_fODwvSU");

        url = googleDirectionsUrl.toString();
        String tag_json_obj = "json_obj_req";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String[] directionsList = _dataParser.parseJSONDirections(response);
                        displayDirection(directionsList);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void displayDirection(String[] directionsList) {
        _googleMap.clear();
        _googleMap.addMarker(new MarkerOptions().position(new LatLng(_placeLatLang.latitude, _placeLatLang.longitude)));

        PolylineOptions options = new PolylineOptions();
        options.color(Color.BLUE);
        options.width(12);

        for (int i = 0; i < directionsList.length; i++) {
            options.addAll(PolyUtil.decode(directionsList[i]));
        }
        _googleMap.addPolyline(options);

        _googleMap.addMarker(new MarkerOptions().position(new LatLng(_latOrigin, _lonOrigin)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(_placeLatLang).zoom(12).build();
        _googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    //endregion
}


