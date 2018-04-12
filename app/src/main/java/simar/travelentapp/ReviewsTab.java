package simar.travelentapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReviewsTab extends Fragment {
    ArrayList<Reviews> _reviewList = new ArrayList<>();
    ArrayList<Reviews> _reviewListYelp = new ArrayList<>();
    ArrayList<Reviews> _reviewListDefaultSort = new ArrayList<>();
    ArrayList<Reviews> _reviewListYelpDefaultSort = new ArrayList<>();
    private AdapterReviews _adapterReviews;
    private AdapterReviews _adapterReviewsYelp;
    View _rootView;
    JSONObject _details = null;
    Spinner _spinnerReviewType;
    Spinner _spinnerReviewSort;
    ProgressDialog _pDialog;
    RecyclerView _recReviews;
    int _reviewPosition = 0;
    int _reviewSortPosition = 0;
    TextView _emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.reviews_tab, container, false);

        _emptyView = (TextView) _rootView.findViewById(R.id.emptyView);
        //Initialize Recycler View
        _recReviews = (RecyclerView) _rootView.findViewById(R.id.recReviews);
        _recReviews.setLayoutManager(new LinearLayoutManager(getActivity()));
        _adapterReviews = new AdapterReviews(getActivity());
        _recReviews.setAdapter(_adapterReviews);

        _recReviews.addOnItemTouchListener(new RecyclerTouchListener(getContext(), _recReviews, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Reviews review = null;
                if (_reviewPosition == 0) {
                    review = _reviewList.get(position);
                } else {
                    review = _reviewListYelp.get(position);
                }
                Uri uri = Uri.parse(review.getRevUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        //Initialize process dialog
        _pDialog = new ProgressDialog(getActivity());
        _pDialog.setMessage("Fetching Data");
        _pDialog.setCancelable(false);

        Bundle bundleDetails = getArguments();
        String detailsString = bundleDetails.getString("Reviews");

        try {
            _details = new JSONObject(detailsString);
            if(_details.has("reviews")){
                JSONArray reviews = _details.getJSONArray("reviews");
                parseJsonData(reviews);
                _adapterReviews.setReviewsList(_reviewList);
            }
            else{
                _recReviews.setVisibility(View.GONE);
                _emptyView.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            _recReviews.setVisibility(View.GONE);
            _emptyView.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), R.string.common_error, Toast.LENGTH_SHORT).show();
        }

        // Spinner Initialization
        _spinnerReviewType = (Spinner) _rootView.findViewById(R.id.spinnerReviewType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this.getActivity(), R.array.review_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerReviewType.setAdapter(adapter);
        _spinnerReviewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                toggleReviewsType(parent, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Spinner Initialization
        _spinnerReviewSort = (Spinner) _rootView.findViewById(R.id.spinnerReviewSort);
        ArrayAdapter<CharSequence> adapterSort = ArrayAdapter.createFromResource(
                this.getActivity(), R.array.review_sort_array, android.R.layout.simple_spinner_item);
        adapterSort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _spinnerReviewSort.setAdapter(adapterSort);
        _spinnerReviewSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortReviews(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do Nothing
            }
        });

        return _rootView;
    }

    private void parseJsonData(JSONArray reviews) throws JSONException {
        for (int i = 0; i < reviews.length(); i++) {
            JSONObject jsonPlace = reviews.getJSONObject(i);

            String revUrl = jsonPlace.getString("author_url");
            String revProfilePic = jsonPlace.getString("profile_photo_url");
            String revName = jsonPlace.getString("author_name");
            String revReview = jsonPlace.getString("text");
            float revRating = Float.parseFloat(jsonPlace.getString("rating"));

            long unixSeconds = Long.parseLong(jsonPlace.getString("time"));
            Date date = new java.util.Date(unixSeconds * 1000L);
            SimpleDateFormat simpleDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDate.setTimeZone(java.util.TimeZone.getTimeZone("GMT-8"));
            Date revDate = null;
            try {
                revDate = simpleDate.parse(simpleDate.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Reviews review = new Reviews(revProfilePic, revName, revDate, revReview, revUrl, revRating);
            _reviewList.add(review);
            _reviewListDefaultSort.add(review);
        }
    }

    private void toggleReviewsType(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0){
            _reviewPosition = 0;
            showGoogleReviews();
            sortReviews(_reviewSortPosition);
        }else{
            _reviewPosition = 1;
            initiateBusinessMatch();
        }
    }

    private void sortReviews(int position) {
        if(_reviewPosition == 0) {
            if (position == 0) {
                _reviewList = _reviewListDefaultSort;
                _reviewSortPosition = 0;
            } else if (position == 1) {
                Collections.sort(_reviewList, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return Float.compare(obj2.getRevRating(), obj1.getRevRating());
                    }
                });
                _reviewSortPosition = 1;
            } else if (position == 2) {
                Collections.sort(_reviewList, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return Float.compare(obj1.getRevRating(), obj2.getRevRating());
                    }
                });
                _reviewSortPosition = 2;
            } else if (position == 3) {
                Collections.sort(_reviewList, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return obj2.getRevDate().compareTo(obj1.getRevDate());
                    }
                });
                _reviewSortPosition = 3;
            } else if (position == 4) {
                Collections.sort(_reviewList, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return obj1.getRevDate().compareTo(obj2.getRevDate());
                    }
                });
                _reviewSortPosition = 4;
            }
            _recReviews.setAdapter(_adapterReviews);
            _adapterReviews.setReviewsList(_reviewList);
        } else{
            if (position == 0) {
                _reviewListYelp = _reviewListYelpDefaultSort;
                _reviewSortPosition = 0;
            } else if (position == 1) {
                Collections.sort(_reviewListYelp, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return Float.compare(obj2.getRevRating(), obj1.getRevRating());
                    }
                });
                _reviewSortPosition = 1;
            } else if (position == 2) {
                Collections.sort(_reviewListYelp, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return Float.compare(obj1.getRevRating(), obj2.getRevRating());
                    }
                });
                _reviewSortPosition = 2;
            } else if (position == 3) {
                Collections.sort(_reviewListYelp, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return obj2.getRevDate().compareTo(obj1.getRevDate());
                    }
                });
                _reviewSortPosition = 3;
            } else if (position == 4) {
                Collections.sort(_reviewListYelp, new Comparator<Reviews>() {
                    @Override
                    public int compare(Reviews obj1, Reviews obj2) {
                        return obj1.getRevDate().compareTo(obj2.getRevDate());
                    }
                });
                _reviewSortPosition = 4;
            }
            _recReviews.setAdapter(_adapterReviewsYelp);
            _adapterReviewsYelp.setReviewsList(_reviewListYelp);
        }
    }

    private void showGoogleReviews(){
        _recReviews.setVisibility(View.VISIBLE);
        _emptyView.setVisibility(View.GONE);
    }

    private void initiateBusinessMatch(){
        if(_reviewListYelp.size() == 0) {
            showpDialog();
            String url = GetUrlBusinessMatch();
            String tag_json_obj = "json_obj_req";

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, (String) null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray businessArray = response.getJSONArray("businesses");
                                JSONObject business = null;
                                if (businessArray.length() > 0) {
                                    business = businessArray.getJSONObject(0);
                                    searchYelpReviews(business.getString("id"));
                                    _recReviews.setVisibility(View.VISIBLE);
                                    _emptyView.setVisibility(View.GONE);

                                } else {
                                    _recReviews.setVisibility(View.GONE);
                                    _emptyView.setVisibility(View.VISIBLE);
                                    hidepDialog();
                                }

                            } catch (JSONException e) {
                                _recReviews.setVisibility(View.GONE);
                                _emptyView.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), R.string.common_error, Toast.LENGTH_SHORT).show();
                                hidepDialog();
                            }
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
        }else{
            _recReviews.setVisibility(View.VISIBLE);
            _emptyView.setVisibility(View.GONE);
            sortReviews(_reviewSortPosition);
        }
    }

    private void searchYelpReviews(String id){
        String url = "http://googleapicalls.us-east-2.elasticbeanstalk.com";
        url += "/yelpreviews?";
        url += "id=" + id;

        String tag_json_obj = "json_obj_req";

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray yelpReviews = response.getJSONArray("reviews");
                            parseYelpReviews(yelpReviews);
                            _adapterReviewsYelp = new AdapterReviews(getActivity());
                            sortReviews(_reviewSortPosition);
                            hidepDialog();

                        } catch (JSONException e) {
                            _recReviews.setVisibility(View.GONE);
                            _emptyView.setVisibility(View.VISIBLE);
                            Toast.makeText(getActivity(), R.string.common_error, Toast.LENGTH_SHORT).show();
                            hidepDialog();
                        }
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

    private String GetUrlBusinessMatch() {
        String url = "http://googleapicalls.us-east-2.elasticbeanstalk.com";
        try {
            url += "/yelpmatch?";
            url += "name=" + _details.getString("name") + "&";

            double latitude = _details.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            double longitude = _details.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

            String city = "";
            String state = "";
            String country = "";
            String address1 = "";
            String postalCode = "";

            JSONArray addressComp = _details.getJSONArray("address_components");
            for (int i = 0; i < addressComp.length(); i++) {
                JSONObject line = addressComp.getJSONObject(i);
                JSONArray typesArray = line.getJSONArray("types");

                JSONObject types = typesArray.toJSONObject(typesArray);

                if (types.has("route")) {
                    address1 = line.getString("long_name");
                } else if (types.has("locality")) {
                    city = line.getString("long_name");
                } else if (types.has("administrative_area_level_1")) {
                    state = line.getString("short_name");
                } else if (types.has("country")) {
                    country = line.getString("short_name");
                } else if (types.has("postal_code")) {
                    postalCode = line.getString("short_name");
                }
            }

            url += "city=" + city + "&";
            url += "state=" + state + "&";
            url += "country=" + country + "&";
            url += "address1=" + address1 + "&";
            url += "postalCode=" + postalCode;

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.common_error, Toast.LENGTH_SHORT).show();
        }
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

    private void parseYelpReviews(JSONArray yelpReviews) throws JSONException {
        for (int i = 0; i < yelpReviews.length(); i++) {
            JSONObject jsonPlace = yelpReviews.getJSONObject(i);

            String revUrl = jsonPlace.getString("url");
            String revProfilePic = jsonPlace.getJSONObject("user").getString("image_url");
            String revName = jsonPlace.getJSONObject("user").getString("name");
            String revReview = jsonPlace.getString("text");
            float revRating = Float.parseFloat(jsonPlace.getString("rating"));

            Date revDate = null;
            try {
                revDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonPlace.getString("time_created"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Reviews review = new Reviews(revProfilePic, revName, revDate, revReview, revUrl, revRating);
            _reviewListYelp.add(review);
            _reviewListYelpDefaultSort.add(review);
        }
    }
}