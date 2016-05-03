package com.nemator.needle.fragments.haystack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.activities.HaystackActivity;
import com.nemator.needle.adapter.PinCardAdapter;
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.PinsResult;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.PinVO;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackPinsTabFragment extends Fragment{
    public static final String TAG = "HaystackPinsTabFragment";

    private View rootView;
    private RecyclerView listView;
    private Button newPinButton;

    private HaystackVO haystack;
    private PinCardAdapter pinAdapter;
    private GridLayoutManager layoutManager;
    private ArrayList<PinVO> pins;

    public HaystackPinsTabFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pins_tab, container, false);

        listView =  (RecyclerView) rootView.findViewById(R.id.list);
        int spanCount = 2;//TODO : define span depending on screen width
        layoutManager = new GridLayoutManager(getContext(), spanCount);
        listView.setLayoutManager(layoutManager);

        newPinButton = (Button) rootView.findViewById(R.id.new_pin_button);
        newPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HaystackActivity) getActivity()).addPin();
            }
        });

        haystack = ((HaystackActivity) getActivity()).getHaystack();

        fetchPins();

        return rootView;
    }

    private PinCardAdapter.PinCardListener cardListener = new PinCardAdapter.PinCardListener() {
        @Override
        public void onClick(PinVO pinVO) {
            Log.d(TAG, "click on pin");        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.d(TAG, "click on item");
            return false;
        }
    };

    private void fetchPins(){
        ApiClient.getInstance().getHaystackPins(haystack.getId(), pinsFetchedCallback);
    }

    private Callback<PinsResult> pinsFetchedCallback = new Callback<PinsResult>(){
        @Override
        public void onResponse(Call<PinsResult> call, Response<PinsResult> response) {
            PinsResult result = response.body();
            Log.d(TAG, result.getPins().size() + " pins fetched");

            pins = result.getPins();
            updateUserList();
        }

        @Override
        public void onFailure(Call<PinsResult> call, Throwable t) {
            Log.d(TAG, "Could not fetch pins : " + t.getMessage());
        }
    };

    private void updateUserList(){
        pinAdapter = new PinCardAdapter(pins, getActivity(), cardListener);
        listView.setAdapter(pinAdapter);

        pinAdapter.notifyDataSetChanged();
        listView.invalidate();
    }

    public void setHaystack(HaystackVO haystack) {
        this.haystack = haystack;
        updateUserList();
    }
}