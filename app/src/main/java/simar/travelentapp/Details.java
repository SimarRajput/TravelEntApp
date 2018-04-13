package simar.travelentapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class Details extends AppCompatActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private JSONObject _details;
    Button _btnAddRemoveFavorite = null;
    DatabaseHelper _databaseHelper = null;
    private int _positionInSearchResults = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.getTabAt(0).setCustomView(R.layout.details_info_tab_item);
        tabLayout.getTabAt(1).setCustomView(R.layout.details_photo_tab_item);
        tabLayout.getTabAt(2).setCustomView(R.layout.details_map_tab_item);
        tabLayout.getTabAt(3).setCustomView(R.layout.details_reviews_tab_item);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        _btnAddRemoveFavorite = (Button) findViewById(R.id.btnFavorite);
        _databaseHelper = new DatabaseHelper(getApplicationContext());

        // Get intent data
        try {
            JSONObject searchResults = null;
            searchResults = new JSONObject(getIntent().getStringExtra("SearchResults"));
            _positionInSearchResults = getIntent().getIntExtra("Position", -1);
            _details = searchResults.getJSONObject("result");
            String placeID = _details.getString("place_id");
            getSupportActionBar().setTitle(_details.getString("name"));
            checkifInFavorite(placeID);

        } catch (JSONException e) {
            Toast.makeText(this, R.string.common_error, Toast.LENGTH_SHORT).show();
        }

        //Initialize toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    InfoTab infoTab = new InfoTab();
                    Bundle args = new Bundle();
                    args.putString("Details", _details.toString());
                    infoTab.setArguments(args);
                    return infoTab;
                case 1:
                    PhotosTab photosTab = new PhotosTab();
                    Bundle argsPhotos = new Bundle();
                    String placeID = "";
                    try {
                        if(_details.has("place_id"))
                            placeID = _details.getString("place_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    argsPhotos.putString("PhotosPlaceID", placeID);
                    photosTab.setArguments(argsPhotos);
                    return photosTab;
                case 2:
                    MapsTab mapsTab = new MapsTab();
                    Bundle latLng = getLatLngBundle();
                    mapsTab.setArguments(latLng);
                    return mapsTab;
                case 3:
                    ReviewsTab reviewsTab = new ReviewsTab();
                    Bundle reviews = getReviews();
                    reviewsTab.setArguments(reviews);
                    return reviewsTab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    private Bundle getLatLngBundle(){
        Bundle args = new Bundle();
        double lat = 0;
        double lng = 0;

        try {
            lat = _details.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            lng = _details.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        args.putString("LatLng", lat + "," + lng);

        return args;
    }

    private Bundle getReviews(){
        Bundle args = new Bundle();
        args.putString("Reviews", _details.toString());
        return args;
    }

    public void shareOnTwitter(View view){
        String twitterText = null;
        try {
            twitterText = "Check out " + _details.get("name");

            if (_details.has("formatted_address"))
                twitterText += " located at " + _details.get("formatted_address")  + ". Website:";

            String twitterUrl = "";
            if (_details.has("website"))
                twitterUrl = _details.getString("website");
            else
                twitterUrl = _details.getString("url");

            String twitterHashtag = "TravelAndEntertainmentSearch";

            twitterText = URLEncoder.encode(twitterText);
            twitterUrl = URLEncoder.encode(twitterUrl);

            twitterText = twitterText.replace("/#/g", "%23");
            twitterUrl = twitterUrl.replace("/#/g", "%23");

            String finalTwitter = "https://twitter.com/share?ref_src=twsrc%5Etfw&text=" + twitterText + "&url=" + twitterUrl + "&hashtags=" + twitterHashtag;

            Intent twitterIntent = new Intent();
            twitterIntent.putExtra(Intent.EXTRA_TEXT, twitterText + "&url=" + twitterUrl + "&hashtags=" + twitterHashtag);
            twitterIntent.setAction(Intent.ACTION_VIEW);
            twitterIntent.setData(Uri.parse(finalTwitter));
            startActivity(twitterIntent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addRemoveFavorite(View view){

        //Add Favorite
        String placeId = "";
        String placeName ="";
        String placeLocation = "";
        String placeIcon = "";

        try {
            placeId = _details.getString("place_id");
            placeName = _details.getString("name");
            placeLocation = _details.getString("formatted_address");
            placeIcon = _details.getString("icon");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(_btnAddRemoveFavorite.getBackground().getConstantState() == view.getResources().getDrawable(R.drawable.heart_outline_white).getConstantState()) {
            boolean resultAdd = _databaseHelper.addData(
                    placeId, placeName,
                    placeLocation, placeIcon,
                    view.getResources().getString(R.string.tableFavorites));

            if (resultAdd) {
                _btnAddRemoveFavorite.setBackgroundResource(R.drawable.heart_white);
                Toast.makeText(view.getContext(), placeName + " was added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean resultDelete = _databaseHelper.deleteData(placeId, view.getResources().getString(R.string.tableFavorites));

            if (resultDelete) {
                _btnAddRemoveFavorite.setBackgroundResource(R.drawable.heart_outline_white);
                Toast.makeText(view.getContext(), placeName + " was deleted from favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), R.string.common_error, Toast.LENGTH_SHORT).show();
            }
        }
        setResult(RESULT_OK, null);
    }

    private void checkifInFavorite(String placeID){
        ArrayList<String> favPlaceIdList = _databaseHelper.getFavoritePlaceIdList();

        if(favPlaceIdList.indexOf(placeID) < 0){
            _btnAddRemoveFavorite.setBackgroundResource(R.drawable.heart_outline_white);
        }else{
            _btnAddRemoveFavorite.setBackgroundResource(R.drawable.heart_white);
        }
    }
}
