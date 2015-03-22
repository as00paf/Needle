package com.nemator.needle.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nemator.needle.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.models.Haystack;
import com.shamanland.fab.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class HaystackFragment extends Fragment {
    private static final String TAG = "HaystackFragment";
    private SlidingUpPanelLayout mLayout;

    public static HaystackFragment newInstance() {
        HaystackFragment fragment = new HaystackFragment();
        return fragment;
    }

    public HaystackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (SlidingUpPanelLayout) inflater.inflate(R.layout.haystack_fragment, container, false);

        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.setClipPanel(false);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(TAG, "onPanelExpanded");

            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });

        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) mLayout.findViewById(R.id.fab_add_users);
        if(((HaystackActivity) getActivity()).isOwner()){
            fab.setSize(FloatingActionButton.SIZE_NORMAL);
            fab.setColor(getResources().getColor(R.color.primary));

            fab.initBackground();
            fab.setImageResource(R.drawable.ic_action_add_person);
            fab.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HaystackActivity) getActivity()).addUsers();
                }
            });
        }else{
            fab.setVisibility(View.INVISIBLE);
        }

        //Active Until Label
        String activeUntil = getResources().getString(R.string.activeUntil)+ " " + ((HaystackActivity) getActivity()).getHaystack().getTimeLimit();
        activeUntil = activeUntil.replace(" 00:00:00", "");
        activeUntil = activeUntil.replace(":00", "");

        TextView activeUntilLabel = (TextView) mLayout.findViewById(R.id.active_until_label);
        activeUntilLabel.setText(activeUntil);

        return mLayout;
    }
}
