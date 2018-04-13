package simar.travelentapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import simar.travelentapp.Adapters.AdapterSearchResults;
import simar.travelentapp.Controllers.AppController;
import simar.travelentapp.HelperClasses.DataParser;
import simar.travelentapp.HelperClasses.DatabaseHelper;
import simar.travelentapp.HelperClasses.Places;
import simar.travelentapp.R;

public class SearchResults extends AppCompatActivity {
    //region Variables
    private ArrayList<Places> _placesList = new ArrayList<>();
    private AdapterSearchResults _adapterSearchResults;
    private DatabaseHelper _databaseHelper;
    private DataParser _dataParser;
    private ProgressDialog _pDialog;

    private int _currentTableNumber = 1;
    private String _nextPageToken = "";

    private Button _btnNext = null;
    private Button _btnPrevious = null;
    private TextView _emptyView;
    private RecyclerView _recResults;
    //endregion

    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        //Intialize views
        _btnNext = (Button) findViewById(R.id.btnNext);
        _btnPrevious = (Button) findViewById(R.id.btnPrevious);
        _emptyView = (TextView) findViewById(R.id.emptyView);

        //Initialize Database and data parser
        _databaseHelper = new DatabaseHelper(getApplicationContext());
        _databaseHelper.deleteSearchResultsData();
        _dataParser = new DataParser();

        //Initialize process dialog
        _pDialog = new ProgressDialog(this);
        _pDialog.setMessage("Fetching Data");
        _pDialog.setCancelable(false);

        //Initialize Recycler View and adapter for recycler view
        _recResults= (RecyclerView) findViewById(R.id.recResults);
        _recResults.setLayoutManager(new LinearLayoutManager(this));
        _adapterSearchResults = new AdapterSearchResults(this);
        _recResults.setAdapter(_adapterSearchResults);

        // Events bubbled from Adapter
        _adapterSearchResults.setOnItemClickListener(new AdapterSearchResults.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Places place = _placesList.get(position);
                showpDialog();
                openDetails(place, position);
            }

            @Override
            public void onFavoriteButtonClick(View view, int position) {
                Button btnFav = (Button) view.findViewById(R.id.btnFav);
                Places place = _placesList.get(position);
                if (btnFav.getBackground().getConstantState() == getResources().getDrawable(R.drawable.heart_outline_black).getConstantState()) {
                    //Add Favorite
                    boolean resultAdd = _databaseHelper.addData(
                            place.getPlaceID(), place.getPlaceName(),
                            place.getPlaceLocation(), place.getPlaceIcon(),
                            getResources().getString(R.string.tableFavorites));

                    if (resultAdd) {
                        btnFav.setBackgroundResource(R.drawable.heart_red);
                        Toast.makeText(getApplicationContext(), place.getPlaceName() + " was added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    boolean resultDelete = _databaseHelper.deleteData(place.getPlaceID(), getResources().getString(R.string.tableFavorites));

                    if (resultDelete) {
                        btnFav.setBackgroundResource(R.drawable.heart_outline_black);
                        Toast.makeText(getApplicationContext(), place.getPlaceName() + " was deleted from favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Initialize toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get intent data
        try {
            JSONObject searchResults = null;
            searchResults = new JSONObject(getIntent().getStringExtra("SearchResults"));

            if(searchResults.has("next_page_token")){
                _nextPageToken = searchResults.getString("next_page_token");
                _btnNext.setEnabled(true);
            }else{
                _btnNext.setEnabled(false);
            }

            _placesList = _dataParser.parseJSONPlaces(searchResults);
            if(_placesList.size() > 0) {
                addPlacesToDatabase(_placesList);
                _adapterSearchResults.setPlacesList(_placesList);
                _recResults.setVisibility(View.VISIBLE);
                _emptyView.setVisibility(View.GONE);
            }else{
                _recResults.setVisibility(View.GONE);
                _emptyView.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            _recResults.setVisibility(View.GONE);
            _emptyView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Unable to get search results. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Intent intent = getIntent();
            startActivity(intent);
            finish();
        }
    }
    //endregion

    //region Private Methods
    private void openDetails(Places place, final int position){
        String url = GetUrl(place.getPlaceID());
        String tag_json_obj = "json_obj_req";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent resultsIntent = new Intent(getBaseContext(), Details.class);
                        resultsIntent.putExtra("SearchResults", response.toString());
                        resultsIntent.putExtra("Position", position);
                        startActivityForResult(resultsIntent, 1);
                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = error.getMessage();
                Toast.makeText(getBaseContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

    private String GetUrl(String placeID) {
        String url = "http://googleapicalls.us-east-2.elasticbeanstalk.com";

        url += "/details?";
        url += "placeId=" + placeID;

        return url;
    }

    private void addPlacesToDatabase(ArrayList<Places> placesList){
        for(Places place: placesList) {
            _databaseHelper.addData(place.getPlaceID(), place.getPlaceName(),
                    place.getPlaceLocation(), place.getPlaceIcon(),
                    getString(R.string.tableSearchResults));
        }
    }
    //endregion

    //region Public Methods
    public void getPreviousResults(View view) {
        int start = 0;
        int end = 0;

        _currentTableNumber -= 1;

        if (_currentTableNumber == 1) {
            start = 1;
            end = 20;
            _btnNext.setEnabled(true);
        } else if (_currentTableNumber == 2) {
            start = 21;
            end = 40;
            _btnNext.setEnabled(true);
        }

        Cursor data = _databaseHelper.getSearchResultsData(start, end);

        if (data.getCount() > 0) {
            _placesList = new ArrayList<>();
            while (data.moveToNext()) {
                String placeID = data.getString(1);
                String placeName = data.getString(2);
                String placeLocation = data.getString(3);
                String placeIcon = data.getString(4);

                Places place = new Places(placeID, placeName, placeLocation, placeIcon);
                _placesList.add(place);
            }
            _adapterSearchResults.setPlacesList(_placesList);

            if (_currentTableNumber == 1) {
                _btnPrevious.setEnabled(false);
            }
        }
    }

    public void getNextResults(View view) {
        showpDialog();
        Cursor data = null;
        if (_currentTableNumber == 1) {
            data = _databaseHelper.getSearchResultsData(21, 40);
        } else if (_currentTableNumber == 2) {
            data = _databaseHelper.getSearchResultsData(41, 60);
        }

        if (data.getCount() > 0) {
            _placesList = new ArrayList<>();
            while (data.moveToNext()) {
                String placeID = data.getString(1);
                String placeName = data.getString(2);
                String placeLocation = data.getString(3);
                String placeIcon = data.getString(4);

                Places place = new Places(placeID, placeName, placeLocation, placeIcon);
                _placesList.add(place);
            }

            _adapterSearchResults.setPlacesList(_placesList);
            _btnNext.setEnabled(true);
            _currentTableNumber += 1;
            if(_currentTableNumber == 3){
                _btnNext.setEnabled(false);
            }
            _btnPrevious.setEnabled(true);
            hidepDialog();

        } else {
            String url = "http://googleapicalls.us-east-2.elasticbeanstalk.com";
            url += "/nextpage?";
            url += "nextPageToken=" + _nextPageToken;

            String tag_json_obj = "json_obj_req";

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, (String) null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                try {
                                    _placesList = new ArrayList<>();
                                    _placesList = _dataParser.parseJSONPlaces(response);
                                    if(_placesList.size() > 0) {
                                        addPlacesToDatabase(_placesList);
                                        _adapterSearchResults.setPlacesList(_placesList);
                                        if (response.has("next_page_token")) {
                                            _nextPageToken = response.getString("next_page_token");
                                            _btnNext.setEnabled(true);
                                        } else {
                                            _btnNext.setEnabled(false);
                                        }
                                        _btnPrevious.setEnabled(true);
                                        _currentTableNumber += 1;
                                        _recResults.setVisibility(View.VISIBLE);
                                        _emptyView.setVisibility(View.GONE);
                                    }else{
                                        _recResults.setVisibility(View.GONE);
                                        _emptyView.setVisibility(View.VISIBLE);
                                    }
                                } catch (JSONException e) {

                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    _recResults.setVisibility(View.GONE);
                                    _emptyView.setVisibility(View.VISIBLE);
                                }
                            }
                            hidepDialog();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String errorMessage = error.getMessage();
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    hidepDialog();
                    _recResults.setVisibility(View.GONE);
                    _emptyView.setVisibility(View.VISIBLE);
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
    }
    //endregion
}
