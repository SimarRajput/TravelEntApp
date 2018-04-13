package simar.travelentapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SearchTab extends Fragment {
    private ProgressDialog _pDialog;
    private View _rootView;
    double _lat;
    double _lon;
    boolean _locationPerGranted = false;
    private static final int _locReqCode = 1;
    private static final int _autoCompleteReqCode = 1;
    protected GeoDataClient _geoDataClient;
    private AdapterAutocomplete _autoCompAdapter;

    EditText _txtKeyword;
    Spinner _spinner;
    EditText _txtDistance;
    RadioGroup _radioGroup;
    RadioButton _rdCurrentLoc;
    AutoCompleteTextView _txtLocation;
    Button _btnSearch;
    Button _btnClear;
    TextInputLayout _txtKeywordLayout;
    TextInputLayout _txtLocationLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.search_tab, container, false);

        // Initialize everything
        initializeForm();
        getCurrentLocation();

        //Add event to radio group
        _radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                handleRadioChecked(group, checkedId);
            }
        });

        //Initialize process dialog
        _pDialog = new ProgressDialog(getActivity());
        _pDialog.setMessage("Fetching Data");
        _pDialog.setCancelable(false);

        // Spinner Initialization
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.getActivity(), R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinner.setAdapter(adapter);

        //Search Button Event
        _btnSearch.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (validateInput()) {
                            showpDialog();
                            searchData();
                        }
                    }
                }
        );

        //Clear Button Event
        _btnClear.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if (validateInput()) {
                            clearForm();
                        }
                    }
                }
        );

        _txtLocation.setOnItemClickListener(mAutocompleteClickListener);
        return _rootView;
    }

    public void searchData() {
        String url = GetUrl();
        String tag_json_obj = "json_obj_req";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent resultsIntent = new Intent(getActivity(), SearchResults.class);
                        resultsIntent.putExtra("SearchResults", response.toString());
                        startActivity(resultsIntent);
                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                hidepDialog();
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

    private void showpDialog() {
        if (!_pDialog.isShowing())
            _pDialog.show();
    }

    private void hidepDialog() {
        if (_pDialog.isShowing())
            _pDialog.dismiss();
    }

    private boolean validateInput() {
        boolean validated = true;

        if (_txtKeyword.getText().toString().matches("")) {
            showErrors(_txtKeywordLayout.getId());
            validated = false;
        }else{
            clearErrors(_txtKeywordLayout.getId());
        }

        if (_rdCurrentLoc.isChecked() == false) {
            if (_txtLocation.getText().toString().matches("")) {
                showErrors(_txtLocationLayout.getId());
                validated = false;
            }
            else{
                clearErrors(_txtLocationLayout.getId());
            }
        }
        return validated;
    }

    private void showErrors(int id) {
        TextInputLayout txtError = (TextInputLayout) _rootView.findViewById(id);
        txtError.requestFocus();
        txtError.setErrorEnabled(true);
        txtError.setError(getText(R.string.req_error));
        Toast.makeText(getActivity(), R.string.req_error_toast, Toast.LENGTH_SHORT).show();
    }

    private void clearErrors(int id) {
        TextInputLayout txtError = (TextInputLayout) _rootView.findViewById(id);
        txtError.requestFocus();
        txtError.setErrorEnabled(false);
        txtError.setError(null);
    }


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fillLocation();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    _locReqCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == _locReqCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fillLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void fillLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, locationListener);
    }

    private String GetUrl() {
        String url = "http://googleapicalls.us-east-2.elasticbeanstalk.com";

        int distance = 0;
        if(_txtDistance.getText().toString().matches("")){
            distance = (int) (10 * 1609.34);
        } else{
            distance = Integer.parseInt(_txtDistance.getText().toString());
            distance = (int) (distance * 1609.34);
        }

        url += "/places?";
        url += "location=" + _lat + "," + _lon + "&";
        url += "radius=" + distance + "&";
        url += "type=" + _spinner.getSelectedItem().toString() + "&";
        url += "keyword=" + _txtKeyword.getText().toString().trim();

        return url;
    }

    private void initializeForm() {
        _radioGroup = (RadioGroup) _rootView.findViewById(R.id.rdGroup);
        _spinner = (Spinner) _rootView.findViewById(R.id.spinner);
        _btnSearch = (Button) _rootView.findViewById(R.id.btnSearch);
        _btnClear = (Button) _rootView.findViewById(R.id.btnClear);
        _txtLocation = (AutoCompleteTextView) _rootView.findViewById(R.id.txtLocation);
        _rdCurrentLoc = (RadioButton) _rootView.findViewById(R.id.rdCurrentLoc);
        _txtKeyword = (EditText) _rootView.findViewById(R.id.txtKeyword);
        _txtDistance = (EditText) _rootView.findViewById(R.id.txtDistance);
        _txtKeywordLayout = (TextInputLayout) _rootView.findViewById(R.id.txtKeywordLayout);
        _txtLocationLayout = (TextInputLayout) _rootView.findViewById(R.id.txtLocationLayout);

        _geoDataClient = Places.getGeoDataClient(getActivity(), null);
        _txtLocation.setEnabled(false);
        _rdCurrentLoc.setChecked(true);

        _autoCompAdapter = new AdapterAutocomplete(getActivity(), _geoDataClient, null, null);
        _txtLocation.setAdapter(_autoCompAdapter);
    }

    private void handleRadioChecked(RadioGroup rdGroup, int checkedId) {
        RadioButton rdChecked = (RadioButton) _rootView.findViewById(checkedId);

        int radioIndex = rdGroup.indexOfChild(rdChecked);
        if (radioIndex == 1) {
            _txtLocation.setEnabled(true);
        } else {
            _txtLocation.setText("");
            _txtLocation.setEnabled(false);
        }
    }

    private void clearForm() {
        _txtKeyword.setText("");
        _txtKeyword.requestFocus();
        _spinner.setSelection(0);
        _txtDistance.setText("");
        _rdCurrentLoc.setChecked(true);
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
                validateInput();
            } catch (RuntimeRemoteException e) {
                return;
            }
        }
    };

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            _lon = loc.getLongitude();
            _lat = loc.getLatitude();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }
}