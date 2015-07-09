package com.nemator.needle.view.haystacks.createHaystack;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.nemator.needle.MainActivity;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.view.haystacks.OnActivityStateChangeListener;

public class CreateHaystackBaseFragment extends Fragment {
    protected View rootView;

    protected OnActivityStateChangeListener stateChangeCallback;

    public HaystackVO haystack(){
        return ((CreateHaystackFragment) getParentFragment()).getHaystack();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = ((OnActivityStateChangeListener) ((MainActivity) getActivity()).getNavigationController());
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActivityStateChangeListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        setHasOptionsMenu(true);
        this.setRetainInstance(true);
    }
}
