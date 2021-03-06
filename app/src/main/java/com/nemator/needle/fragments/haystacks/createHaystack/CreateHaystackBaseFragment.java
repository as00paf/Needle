package com.nemator.needle.fragments.haystacks.createHaystack;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.nemator.needle.Needle;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.fragments.haystacks.OnActivityStateChangeListener;

public class CreateHaystackBaseFragment extends Fragment {
    protected View rootView;

    protected OnActivityStateChangeListener stateChangeCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            stateChangeCallback = Needle.navigationController;
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
