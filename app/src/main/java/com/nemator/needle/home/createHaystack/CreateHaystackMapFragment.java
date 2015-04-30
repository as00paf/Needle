package com.nemator.needle.home.createHaystack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.R;

import java.util.ArrayList;

public class CreateHaystackMapFragment extends CreateHaystackBaseFragment{
    public View rootView;

    private GoogleMap mMap;

    Boolean mIsMapMoveable = false;

    public static boolean mMapIsTouched = false;
    Projection projection;
    public double latitude;
    public double longitude;
    ArrayList<LatLng> val = new ArrayList<LatLng>();
    private float mScaleFactor = 1.f;
    private CreateHaystackMap mMapFragment;
    private ScaleGestureDetector mScaleDetector;
    private Boolean mIsCircle = true;

    public static CreateHaystackMapFragment newInstance() {
        CreateHaystackMapFragment fragment = new CreateHaystackMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_map, container, false);

        //Map Fragment
        mMapFragment = (CreateHaystackMap) getChildFragmentManager().findFragmentById(R.id.create_haystack_map);

        mScaleDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());

        FrameLayout mapFrame = (FrameLayout) rootView.findViewById(R.id.create_haystack_map_frame);
        mapFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                if (mIsMapMoveable == true) {
                    return true;
                } else {
                    return false;
                }
            }
            });

        ImageButton btn_draw_State = (ImageButton) rootView.findViewById(R.id.btn_draw_State);
        btn_draw_State.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsMapMoveable != true) {
                    mIsMapMoveable = true;
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_edit_black_24dp));
                } else {
                    mIsMapMoveable = false;
                  //  ((Button) v).setText("Free Draw");
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.ic_vpn_lock_black_24dp));
                }

            }
        });

        ImageButton btnPolygon = (ImageButton) rootView.findViewById(R.id.btn_polygon);
        btnPolygon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCircle != true) {
                    mIsCircle = true;
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.square24));
                } else {
                    mIsCircle = false;
                    ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.circle24));
                }

                mMapFragment.setIsPolygonCircle(mIsCircle);
            }
        });

        return rootView;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            mMapFragment.scaleFactor(mScaleFactor);
            return true;
        }
    }
}
