package com.nemator.needle.tasks.getAutoCompleteResultsTask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.PlaceAutoCompleteVO;
import com.nemator.needle.utils.SphericalUtil;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alex on 14/05/2015.
 */
public class GetAutoCompleteResultsTask extends AsyncTask<Void, Void, Void> {
    public static String TAG = "GetAutoCompleteResultsTask";

    private GetAutoCompleteResultsParams mParams;
    private LatLngBounds mBounds;
    private ArrayList resultList;

    public GetAutoCompleteResultsTask(GetAutoCompleteResultsParams params){
        mParams = params;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... args) {
        String constraint = mParams.getSearchTerm();
        if (mParams.getGoogleApiClient().isConnected()) {
            Log.i(TAG, "Starting autocomplete query for: " + constraint);



            LatLng southWest = SphericalUtil.computeOffset(mParams.getCurrentPosition(), mParams.getZoneRadius(), 135);
            LatLng northEast = SphericalUtil.computeOffset(mParams.getCurrentPosition(), mParams.getZoneRadius(), 315);
            mBounds = new LatLngBounds(southWest, northEast);

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mParams.getGoogleApiClient(), constraint.toString(),
                                    mBounds, mParams.getFilter());

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final com.google.android.gms.common.api.Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());

                    /*Toast.makeText(getActivity(), "Error contacting API: " + status.toString(),
                            Toast.LENGTH_SHORT).show();*/

                autocompletePredictions.release();
                return null;
            }

            Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            // Copy the results into our own data structure, because we can't hold onto the buffer.
            // AutocompletePrediction objects encapsulate the API response (place ID and description).

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                // Get the details of this prediction and copy it into a new PlaceAutocomplete object.
                resultList.add(new PlaceAutoCompleteVO(prediction.getPlaceId(),
                        prediction.getDescription()));
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();

            return null;
        }
        Log.e(TAG, "Google API client is not connected for autocomplete query.");

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(resultList == null || resultList.size() == 0){
            super.onPostExecute(aVoid);
            return;
        }

        ArrayList<PlaceAutoCompleteVO> autoCompleteResults = resultList;
        for (int i = 0; i < autoCompleteResults.size(); i++) {
            String searchTerm = autoCompleteResults.get(i).description.toString();
            SearchResult searchResult = new SearchResult(searchTerm, mParams.getContext().getResources().getDrawable(R.drawable.ic_action_place));
            if(!mParams.getSearchBox().getSearchables().contains(searchResult)) {
                mParams.getSearchBox().addSearchable(searchResult);

            }
        }

        super.onPostExecute(aVoid);
    }
}
