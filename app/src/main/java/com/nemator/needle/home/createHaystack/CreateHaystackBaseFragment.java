package com.nemator.needle.home.createHaystack;

import android.support.v4.app.Fragment;

import com.nemator.needle.models.Haystack;

public class CreateHaystackBaseFragment extends Fragment {
    public Haystack haystack(){
        return ((CreateHaystackFragment) getParentFragment()).getHaystack();
    }
}
