package simar.travelentapp.Tabs;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import simar.travelentapp.R;

public class InfoTab extends Fragment {
    private JSONObject _detailsObject;
    View _rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _rootView = inflater.inflate(R.layout.info_tab, container, false);

        Bundle bundleDetails = getArguments();
        String detailsString = bundleDetails.getString("Details");

        try {
            _detailsObject = new JSONObject(detailsString);
            showInfo();
        } catch (JSONException e) {
            Toast.makeText(getActivity(), R.string.common_error, Toast.LENGTH_SHORT).show();
        }

        return _rootView;
    }

    private void showInfo() throws JSONException {
        LinearLayout relLayout = (LinearLayout) _rootView.findViewById(R.id.relInfoLayout);
        String address = "";
        if (_detailsObject.has("formatted_address")) {
            address = _detailsObject.getString("formatted_address");
            LinearLayout itemsLayout = getHorizontalLayout();
            TextView[] txtAddressList = getTextView(0);

            txtAddressList[0].setText(R.string.txtAddress);
            txtAddressList[0].setTypeface(null, Typeface.BOLD);
            itemsLayout.addView(txtAddressList[0]);

            txtAddressList[1].setText(address);
            itemsLayout.addView(txtAddressList[1]);

            relLayout.addView(itemsLayout);
        }

        String phoneNumber = "";
        if (_detailsObject.has("international_phone_number")) {
            phoneNumber = _detailsObject.getString("international_phone_number");

            LinearLayout itemsLayout = getHorizontalLayout();
            TextView[] txtPhoneNumber = getTextView(50);

            txtPhoneNumber[0].setText(R.string.txtPhone);
            txtPhoneNumber[0].setTypeface(null, Typeface.BOLD);
            itemsLayout.addView(txtPhoneNumber[0]);

            txtPhoneNumber[1].setText(phoneNumber);
            Linkify.addLinks(txtPhoneNumber[1], Linkify.PHONE_NUMBERS);
            txtPhoneNumber[1].setLinksClickable(true);
            itemsLayout.addView(txtPhoneNumber[1]);

            relLayout.addView(itemsLayout);
        }

        int priceLevel = 0;
        if (_detailsObject.has("price_level")) {
            priceLevel = Integer.parseInt(_detailsObject.getString("price_level"));
            String priceLevelDollar = "";

            if(priceLevel > 0) {
                for (int i = 0; i < priceLevel; i++) {
                    priceLevelDollar += "$";
                }
            }

            LinearLayout itemsLayout = getHorizontalLayout();
            TextView[] txtPriceLevel = getTextView(50);

            txtPriceLevel[0].setText(R.string.txtPiceLevel);
            txtPriceLevel[0].setTypeface(null, Typeface.BOLD);
            itemsLayout.addView(txtPriceLevel[0]);

            txtPriceLevel[1].setText(priceLevelDollar);
            itemsLayout.addView(txtPriceLevel[1]);

            relLayout.addView(itemsLayout);
        }

        float rating = 0;
        if (_detailsObject.has("rating")) {
            rating = Float.parseFloat(_detailsObject.getString("rating"));

            LinearLayout itemsLayout = new LinearLayout(getActivity());

            itemsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    130));
            itemsLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView[] txtPriceLevel = getTextView(60);

            txtPriceLevel[0].setText(R.string.txtRating);
            txtPriceLevel[0].setTypeface(null, Typeface.BOLD);
            itemsLayout.addView(txtPriceLevel[0]);

            RatingBar ratingBar = new RatingBar(getActivity());
            ratingBar.setRating(rating);
            ratingBar.setScaleX(0.5f);
            ratingBar.setScaleY(0.5f);
            ratingBar.setIsIndicator(true);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(-115,20,0,0);
            ratingBar.setLayoutParams(layoutParams);

            itemsLayout.addView(ratingBar);
            relLayout.addView(itemsLayout);
        }

        String url = "";
        if (_detailsObject.has("url")) {
            url = _detailsObject.getString("url");

            LinearLayout itemsLayout = getHorizontalLayout();
            TextView[] txtUrl = getTextView(50);

            txtUrl[0].setText(R.string.txtGoogle);
            txtUrl[0].setTypeface(null, Typeface.BOLD);
            itemsLayout.addView(txtUrl[0]);

            txtUrl[1].setText(url);
            Linkify.addLinks(txtUrl[1], Linkify.WEB_URLS);
            txtUrl[1].setLinksClickable(true);
            itemsLayout.addView(txtUrl[1]);

            relLayout.addView(itemsLayout);
        }

        String website = "";
        if (_detailsObject.has("website")) {
            website = _detailsObject.getString("website");

            LinearLayout itemsLayout = getHorizontalLayout();
            TextView[] txtWebsite = getTextView(50);

            txtWebsite[0].setText(R.string.txtWebsite);
            txtWebsite[0].setTypeface(null, Typeface.BOLD);
            itemsLayout.addView(txtWebsite[0]);

            txtWebsite[1].setText(website);
            Linkify.addLinks(txtWebsite[1], Linkify.WEB_URLS);
            txtWebsite[1].setLinksClickable(true);
            itemsLayout.addView(txtWebsite[1]);

            relLayout.addView(itemsLayout);
        }
    }

    private TextView[] getTextView(int margin) {
        TextView[] txtViewList = new TextView[2];

        txtViewList[0] = new TextView(getActivity());
        txtViewList[0].setId(1);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                300,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, margin , 0, 0);
        txtViewList[0].setLayoutParams(layoutParams);

        txtViewList[1] = new TextView(getActivity());

        layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.RIGHT_OF, txtViewList[0].getId());

        layoutParams.setMargins(50, margin, 0, 0);
        txtViewList[1].setLayoutParams(layoutParams);

        return txtViewList;
    }

    private LinearLayout getHorizontalLayout(){
        LinearLayout itemsLayout = new LinearLayout(getActivity());

        itemsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        itemsLayout.setOrientation(LinearLayout.HORIZONTAL);

        return itemsLayout;
    }
}