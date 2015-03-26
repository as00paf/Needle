package com.nemator.needle.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.models.Haystack;
import com.shamanland.fab.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class HaystackFragment extends Fragment {
    private static final String TAG = "HaystackFragment";

    private SlidingUpPanelLayout mLayout;
    SlidingPanelPagerAdapter mSlidingPanelPagerAdapter;
    ViewPager slidingPanelViewPager;
    SlidingTabLayout mSlidingTabLayout;

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

        //Active Until Label
        String activeUntil = getResources().getString(R.string.activeUntil)+ " " + ((HaystackActivity) getActivity()).getHaystack().getTimeLimit();
        activeUntil = activeUntil.replace(" 00:00:00", "");
        activeUntil = activeUntil.replace(":00", "");

        TextView activeUntilLabel = (TextView) mLayout.findViewById(R.id.active_until_label);
        activeUntilLabel.setText(activeUntil);

        //View pager
        mSlidingPanelPagerAdapter = new SlidingPanelPagerAdapter(getActivity().getSupportFragmentManager());
        slidingPanelViewPager = (ViewPager) mLayout.findViewById(R.id.slidingPanelViewPager);
        slidingPanelViewPager.setAdapter(mSlidingPanelPagerAdapter);

        //Tabs
        mSlidingTabLayout = (SlidingTabLayout) mLayout.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mSlidingTabLayout.setCustomTabView(R.layout.icon_tab, R.id.icon_tab_label, R.id.icon_tab_icon);

        mSlidingTabLayout.setViewPager(slidingPanelViewPager);

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

        return mLayout;
    }

    public class SlidingPanelPagerAdapter extends FragmentStatePagerAdapter {
        public SlidingPanelPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title;
            switch (position){
                case 0:
                    title = getString(R.string.title_pins);
                    break;
                case 1:
                    title = getString(R.string.title_directions);
                    break;
                case 2:
                    title = getString(R.string.title_users);
                    break;
                case 3:
                    title = getString(R.string.title_leave);
                    break;
                default:
                    title = "tab " + String.valueOf(position);
                    break;
            }

            return title;
        }

        public Drawable getPageIcon(int position){
            Drawable icon;
            switch (position){
                case 0:
                    icon = getResources().getDrawable(R.drawable.ic_action_place);
                    break;
                case 1:
                    icon= getResources().getDrawable(R.drawable.ic_action_directions);
                    break;
                case 2:
                    icon= getResources().getDrawable(R.drawable.ic_action_group);
                    break;
                case 3:
                    icon= getResources().getDrawable(R.drawable.ic_action_exit);
                    break;
                default:
                    icon = getResources().getDrawable(R.drawable.ic_action_directions);
                    break;
            }

            return icon;
        }
    }

    public static class DemoObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_collection_object, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(R.id.view_pager_label)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));
            return rootView;
        }
    }
}




