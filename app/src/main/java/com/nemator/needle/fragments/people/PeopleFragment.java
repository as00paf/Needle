package com.nemator.needle.fragments.people;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;

/**
 * Created by Alex on 26/11/2015.
 */
public class PeopleFragment extends Fragment {
    public static final String TAG = "peopleFragment";

    private View rootView;

    public PeopleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_people, container);

        return rootView;
    }
}
