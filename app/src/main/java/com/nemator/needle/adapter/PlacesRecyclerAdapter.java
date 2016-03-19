package com.nemator.needle.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.api.NeedleApiClient;
import com.nemator.needle.models.vo.CustomPlace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alex on 29/11/2015.
 */
public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.DataHolder> implements Filterable {
    private static final String TAG = "PlacesRecyclerAdapter";

    public static final int TYPE_HISTORY_HEADER = 0;
    public static final int TYPE_NEARBY_HEADER = 1;
    public static final int TYPE_RESULTS_HEADER = 2;
    public static final int TYPE_HISTORY_ITEM = 3;
    public static final int TYPE_PLACE_ITEM = 4;

    public static final int DEFAULT_MODE = 0;
    public static final int SEARCH_MODE = 1;
    private Activity activity;

    private ArrayList<CustomPlace> searchHistory;
    private ArrayList<CustomPlace> nearbyLocations;
    private ArrayList<CustomPlace> searchResults;
    private NearbyPlaceCardClickListener listener;

    private int mode = DEFAULT_MODE;

    private LatLngBounds bounds;
    private final AutocompleteFilter placeFilter;

    public PlacesRecyclerAdapter(Activity activity,  ArrayList<CustomPlace> nearbyLocations, ArrayList<CustomPlace> searchHistory, NearbyPlaceCardClickListener listener, AutocompleteFilter filter) {
        super();

        this.activity = activity;
        this.searchHistory = searchHistory;
        this.nearbyLocations = nearbyLocations;
        this.listener = listener;
        this.placeFilter = filter;
    }

    @Override
    public int getItemViewType(int position) {
        switch(position){
            case 0:
                return mode == DEFAULT_MODE ? TYPE_NEARBY_HEADER : TYPE_RESULTS_HEADER;
            case 6:
                return TYPE_HISTORY_HEADER;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return TYPE_HISTORY_ITEM;
            default:
                return TYPE_PLACE_ITEM;
        }
    }

    @Override
    public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        DataHolder viewHolder = null;

        switch(viewType){
            case TYPE_NEARBY_HEADER:
            case TYPE_HISTORY_HEADER:
            case TYPE_RESULTS_HEADER:
                v = LayoutInflater.from(activity).inflate(R.layout.place_list_header, parent, false);
                viewHolder = new HeaderDataHolder(v, viewType);
                break;
            case TYPE_PLACE_ITEM:
                v = LayoutInflater.from(activity).inflate(R.layout.card_nearby_place, parent, false);
                viewHolder = new PlaceDataHolder(v, listener, viewType);
                break;
            case TYPE_HISTORY_ITEM:
                v = LayoutInflater.from(activity).inflate(R.layout.card_search_history, parent, false);
                viewHolder = new PlaceDataHolder(v, listener, viewType);
                break;
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DataHolder holder, int position) {
        int viewType = getItemViewType(position);
        CustomPlace place;

        switch(viewType){
            case TYPE_NEARBY_HEADER:
                ((HeaderDataHolder) holder).title.setText(activity.getString(R.string.nearby_places));
                break;
            case TYPE_HISTORY_HEADER:
                ((HeaderDataHolder) holder).title.setText(activity.getString(R.string.recent_places));
                break;
            case TYPE_RESULTS_HEADER:
                ((HeaderDataHolder) holder).title.setText(activity.getString(R.string.search_results));
                break;
            case TYPE_PLACE_ITEM:
                if(mode == DEFAULT_MODE){
                    if(nearbyLocations != null){
                        place = nearbyLocations.get(position - 1);
                    }else{
                        return;
                    }
                }else{
                    place = searchResults.get(position - 1);
                }

                ((PlaceDataHolder) holder).title.setText(place.getTitle());
                ((PlaceDataHolder) holder).setPlace(place);
                break;
            case TYPE_HISTORY_ITEM:
                place = searchHistory.get(position - (nearbyLocations.size() + 2));
                ((PlaceDataHolder) holder).title.setText(place.getTitle());
                ((PlaceDataHolder) holder).setPlace(place);
                break;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;

        if(mode == DEFAULT_MODE){
            int nearbyLocationsCount = (nearbyLocations != null) ? nearbyLocations.size() : 0;

            if(searchHistory != null){
                count = nearbyLocationsCount + searchHistory.size() + (searchHistory.size() > 0 ? 2 : 1);
            }
        }else if(searchResults != null){
            count = searchResults.size() + 1;
        }

        return count;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public class PlaceDataHolder extends DataHolder implements View.OnClickListener {
        public TextView title;
        private NearbyPlaceCardClickListener listener;
        private CustomPlace place;

        public PlaceDataHolder(View itemView, NearbyPlaceCardClickListener listener, int viewType) {
            super(itemView, viewType);
            this.listener = listener;


            title = (TextView) itemView.findViewById(R.id.title_label);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(place != null){
                listener.onClick(place, mode == SEARCH_MODE);
            }
        }

        public void setPlace(CustomPlace place) {
            this.place = place;
        }
    }

    public class HeaderDataHolder extends DataHolder{
        private TextView title;

        public HeaderDataHolder(View itemView, int viewType) {
            super(itemView, viewType);

            title = (TextView) itemView.findViewById(R.id.header_label);
        }
    }


    public interface NearbyPlaceCardClickListener{
        void onClick(CustomPlace place, boolean saveToHistory);
    }

    public class DataHolder extends RecyclerView.ViewHolder{

        protected int viewType;

        public DataHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    // Query the autocomplete API for the entered constraint
                    //searchResults =  Needle.googleApiController.getPredictions(constraint, PlacesRecyclerAdapter.this, bounds, placeFilter);
                    searchResults = getPredictions(constraint);

                    if (searchResults != null) {
                        // Results
                        results.values = searchResults;
                        results.count = searchResults.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetChanged();
                }
            }
        };
    }

    private ArrayList<CustomPlace> getPredictions(CharSequence constraint) {
        if (Needle.googleApiController.getGoogleApiClient() != null) {
            if(!Needle.googleApiController.isConnected()){
                Log.e(TAG, "Google API client is not connected.");
                return null;
            }


            Log.i(TAG, "Executing autocomplete query for: " + constraint);
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(Needle.googleApiController.getGoogleApiClient(), constraint.toString(),
                                    bounds, placeFilter);
            // Wait for predictions, set the timeout.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(activity, "Error: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting place predictions: " + status
                        .toString());
                autocompletePredictions.release();
                return null;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                CustomPlace place = new CustomPlace(prediction.getPlaceId(), prediction.getPrimaryText(null).toString(),
                        prediction.getSecondaryText(null).toString(), null);

                resultList.add(place);
            }
            // Buffer release
            autocompletePredictions.release();

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });

            return resultList;
        }
        Log.e(TAG, "Google API client is not set.");
        return null;
    }

    public void setBounds(LatLngBounds bounds) {
        this.bounds = bounds;
    }
}