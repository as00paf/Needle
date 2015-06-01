package com.nemator.needle.view.haystacks.createHaystack;

import android.support.v4.app.Fragment;

import com.nemator.needle.models.vo.HaystackVO;

public class CreateHaystackBaseFragment extends Fragment {
    public HaystackVO haystack(){
        return ((CreateHaystackFragment) getParentFragment()).getHaystack();
    }
}
