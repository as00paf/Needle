package com.nemator.needle.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.SlidingPanelPagerAdapter;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.views.CustomBottomSheetLayout;

public class HaystackActivity extends AppCompatActivity{

    private static final String TAG = "HaystackActivity";

    private Toolbar toolbar;
    private SlidingPanelPagerAdapter mSlidingPanelPagerAdapter;
    private ViewPager slidingPanelViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private CustomBottomSheetLayout bottomSheet;
    private TextView activeUntilLabel;

    private HaystackVO haystack;
    private boolean mIsOwner;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_haystack);

        if(savedInstanceState != null){
            if (savedInstanceState.keySet().contains(AppConstants.HAYSTACK_DATA_KEY)) {
                haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }

            if (savedInstanceState.keySet().contains(AppConstants.TAG_IS_OWNER)) {
                mIsOwner = savedInstanceState.getBoolean(AppConstants.TAG_IS_OWNER);
            }
        }else{
            haystack = (HaystackVO) getIntent().getExtras().get(AppConstants.TAG_HAYSTACK);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(haystack.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bottomSheet = (CustomBottomSheetLayout) findViewById(R.id.bottomsheet);
        bottomSheet.showWithSheetView(getLayoutInflater().inflate(R.layout.haystack_sliding_tab, bottomSheet, false));
        bottomSheet.setShouldDimContentView(false);
        bottomSheet.setPeekOnDismiss(true);
        bottomSheet.setIsDismissableOnTouch(false);
        bottomSheet.setInterceptContentTouch(false);

        //Active Until Label
        String activeUntil = getResources().getString(R.string.activeUntil)+ " " + haystack.getTimeLimit();
        activeUntil = activeUntil.replace(" 00:00:00", "");
        activeUntil = activeUntil.replace(":00", "");

        activeUntilLabel = (TextView) bottomSheet.findViewById(R.id.active_until_label);
        activeUntilLabel.setText(activeUntil);

        //View pager
        mSlidingPanelPagerAdapter = new SlidingPanelPagerAdapter(getSupportFragmentManager(), this);
        slidingPanelViewPager = (ViewPager) bottomSheet.findViewById(R.id.slidingPanelViewPager);
        slidingPanelViewPager.setAdapter(mSlidingPanelPagerAdapter);

        //Tabs
        mSlidingTabLayout = (SlidingTabLayout) bottomSheet.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        mSlidingTabLayout.setCustomTabView(R.layout.icon_tab, R.id.icon_tab_label, R.id.icon_tab_icon);

        mSlidingTabLayout.setViewPager(slidingPanelViewPager);

        mSlidingTabLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingTabLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                LinearLayout content = (LinearLayout) findViewById(R.id.content);
                int topMargin = ((ViewGroup.MarginLayoutParams) content.getLayoutParams()).topMargin;
                bottomSheet.setPeekSheetTranslation(activeUntilLabel.getHeight() + mSlidingTabLayout.getHeight() + topMargin - 8);
            }
        });

        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) bottomSheet.findViewById(R.id.fab_add_users);
        if(isOwner()){
            fab.setImageResource(R.drawable.ic_person_add_black_24dp);
            fab.setVisibility(View.VISIBLE);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else{
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.haystack, menu);
        return true;
    }

    public boolean isOwner(){
        int userId = Needle.userModel.getUserId();
        int ownerId = haystack.getOwner();

        mIsOwner = userId == ownerId;

        return mIsOwner;
    }

    public HaystackVO getHaystack() {
        return haystack;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Needle.networkController.unregister();
        Needle.serviceController.unbindService();
    }
}
