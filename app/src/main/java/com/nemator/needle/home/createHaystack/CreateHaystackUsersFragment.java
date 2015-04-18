package com.nemator.needle.home.createHaystack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;

public class CreateHaystackUsersFragment extends Fragment{

    public static final String TAG = "CreateHaystackUsersFragment";

    private View rootView;

    public static CreateHaystackUsersFragment newInstance() {
        CreateHaystackUsersFragment fragment = new CreateHaystackUsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackUsersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_users, container, false);

        return rootView;
    }
}
