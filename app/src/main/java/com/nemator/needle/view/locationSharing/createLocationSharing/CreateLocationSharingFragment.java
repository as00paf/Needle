package com.nemator.needle.view.locationSharing.createLocationSharing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;

public class CreateLocationSharingFragment extends Fragment {
    public static String TAG = "CreateLocationSharingFragment";

    private View rootView;

    public static CreateLocationSharingFragment newInstance() {
        CreateLocationSharingFragment fragment = new CreateLocationSharingFragment();
        return fragment;
    }

    public CreateLocationSharingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_location_sharing, container, false);

        return rootView;
    }
}
