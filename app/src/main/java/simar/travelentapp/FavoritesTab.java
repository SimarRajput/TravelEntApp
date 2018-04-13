package simar.travelentapp;

/**
 * Created by simar on 4/6/18.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoritesTab extends Fragment {
    private AdapterSearchResults _adapterSearchResults;
    ArrayList<Places> _placesList = new ArrayList<>();
    ProgressDialog _pDialog;
    private DatabaseHelper _databaseHelper;
    TextView _emptyView;
    RecyclerView _recResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.favorites_tab, container, false);

        // Initialize empty view
        _emptyView = (TextView) rootView.findViewById(R.id.emptyView);

        //Initialize Database
        _databaseHelper = new DatabaseHelper(getActivity());

        //Initialize process dialog
        _pDialog = new ProgressDialog(this.getActivity());
        _pDialog.setMessage("Fetching Data");
        _pDialog.setCancelable(false);

        //Initialize Recycler View
        _recResults = (RecyclerView) rootView.findViewById(R.id.recFavorite);
        _recResults.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        _adapterSearchResults = new AdapterSearchResults(this.getActivity());

        //Get Data from database and show in recycler
        showPlacesInRecycler();

        //Configure Recycler View
        _recResults.setAdapter(_adapterSearchResults);
        _adapterSearchResults.setOnItemClickListener(new AdapterSearchResults.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Places place = _placesList.get(position);
                showpDialog();
                openDetails(place);
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
                        Toast.makeText(getContext(), place.getPlaceName() + " was added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    boolean resultDelete = _databaseHelper.deleteData(place.getPlaceID(), getResources().getString(R.string.tableFavorites));

                    if (resultDelete) {
                        btnFav.setBackgroundResource(R.drawable.heart_outline_black);
                        Toast.makeText(getContext(), place.getPlaceName() + " was deleted from favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
                    }
                }
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(FavoritesTab.this).attach(FavoritesTab.this).commit();
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(_databaseHelper != null) {
            _placesList = new ArrayList<>();
            showPlacesInRecycler();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(FavoritesTab.this).attach(FavoritesTab.this).commit();
        }
    }

    private void showPlacesInRecycler(){
        _placesList = _databaseHelper.getFavoriteData();

        if(_placesList.size() == 0){
            _recResults.setVisibility(View.GONE);
            _emptyView.setVisibility(View.VISIBLE);
        }
        else {
            _adapterSearchResults.setPlacesList(_placesList);
            _recResults.setVisibility(View.VISIBLE);
            _emptyView.setVisibility(View.GONE);
        }
    }
    private void openDetails(Places place){
        String url = GetUrl(place.getPlaceID());
        String tag_json_obj = "json_obj_req";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent resultsIntent = new Intent(getActivity(), Details.class);
                        resultsIntent.putExtra("SearchResults", response.toString());
                        startActivityForResult(resultsIntent, 1);
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


    private String GetUrl(String placeID) {
        String url = "http://googleapicalls.us-east-2.elasticbeanstalk.com";

        url += "/details?";
        url += "placeId=" + placeID;

        return url;
    }

    private void showpDialog() {
        if (!_pDialog.isShowing())
            _pDialog.show();
    }

    private void hidepDialog() {
        if (_pDialog.isShowing())
            _pDialog.dismiss();
    }
}
