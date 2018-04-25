package simar.travelentapp.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import simar.travelentapp.R;

public class AdapterAutocomplete extends ArrayAdapter<AutocompletePrediction> implements Filterable {

    private static final String TAG = "PlaceAutocompleteAdapter";
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<AutocompletePrediction> mResultList;
    private GeoDataClient mGeoDataClient;
    private LatLngBounds mBounds;
    private AutocompleteFilter mPlaceFilter;

    public AdapterAutocomplete(Context context, GeoDataClient geoDataClient,
                                    LatLngBounds bounds, AutocompleteFilter filter) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        mGeoDataClient = geoDataClient;
        mBounds = bounds;
        mPlaceFilter = filter;
    }

    public void setBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        return mResultList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        AutocompletePrediction item = getItem(position);

        if (position == (mResultList.size() - 1)) {
            TextView textView1 = (TextView) row.findViewById(android.R.id.text1);
            textView1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.powered_by_google_light, 0, 0, 0);
            textView1.setText("");
            TextView textView2 = (TextView) row.findViewById(android.R.id.text2);
            textView2.setText("");

        } else{
            TextView textView1 = (TextView) row.findViewById(android.R.id.text1);
            textView1.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            TextView textView2 = (TextView) row.findViewById(android.R.id.text2);
            textView1.setText(item.getPrimaryText(STYLE_BOLD));
            textView2.setText(item.getSecondaryText(STYLE_BOLD));
        }
        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                if (constraint != null) {
                    filterData = getAutocomplete(constraint);
                }

                AutocompletePrediction poweredByGoogle = new AutocompletePrediction() {
                    @Override
                    public CharSequence getFullText(@Nullable CharacterStyle characterStyle) {
                        return null;
                    }

                    @Override
                    public CharSequence getPrimaryText(@Nullable CharacterStyle characterStyle) {
                        return null;
                    }

                    @Override
                    public CharSequence getSecondaryText(@Nullable CharacterStyle characterStyle) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getPlaceId() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public List<Integer> getPlaceTypes() {
                        return null;
                    }

                    @Override
                    public AutocompletePrediction freeze() {
                        return null;
                    }

                    @Override
                    public boolean isDataValid() {
                        return false;
                    }
                };

                filterData.add(poweredByGoogle);
                results.values = filterData;

                if (filterData != null) {
                    results.count = filterData.size();
                } else {
                    results.count = 0;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (results != null && results.count > 0) {
                    mResultList = (ArrayList<AutocompletePrediction>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
        Log.i("AutoComplete", "Starting autocomplete query for: " + constraint);
        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(constraint.toString(), mBounds,
                        mPlaceFilter);
        try {
            Tasks.await(results, 60, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        try {
            AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        } catch (RuntimeExecutionException e) {
            Toast.makeText(getContext(), "Error contacting API: " + e.toString(),
                    Toast.LENGTH_SHORT).show();
            Log.e("AutoComplete", "Error getting autocomplete prediction API call", e);
            return null;
        }
    }
}
