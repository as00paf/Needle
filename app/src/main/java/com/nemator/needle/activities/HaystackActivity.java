package com.nemator.needle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appcompat.view.slidingTab.SlidingTabLayout;
import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.adapter.SlidingPanelPagerAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.HaystackResult;
import com.nemator.needle.fragments.haystack.HaystackUsersTabFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.views.CustomBottomSheetLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HaystackActivity extends AppCompatActivity{

    private static final String TAG = "HaystackActivity";

    private Toolbar toolbar;
    private SlidingPanelPagerAdapter pagerAdapter;
    private ViewPager slidingPanelViewPager;
    private SlidingTabLayout tabLayout;
    private CustomBottomSheetLayout bottomSheet;
    private TextView activeUntilLabel;
    private FloatingActionButton fab;

    private HaystackVO haystack;
    private boolean mIsOwner;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_haystack);

        if(savedInstanceState != null){
            if (savedInstanceState.keySet().contains(AppConstants.TAG_HAYSTACK)) {
                haystack = savedInstanceState.getParcelable(AppConstants.TAG_HAYSTACK);
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
        pagerAdapter = new SlidingPanelPagerAdapter(getSupportFragmentManager(), this);
        slidingPanelViewPager = (ViewPager) bottomSheet.findViewById(R.id.slidingPanelViewPager);
        slidingPanelViewPager.setAdapter(pagerAdapter);

        //Tabs
        tabLayout = (SlidingTabLayout) bottomSheet.findViewById(R.id.sliding_tabs);
        tabLayout.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        tabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabLayout.setCustomTabView(R.layout.icon_tab, R.id.icon_tab_label, R.id.icon_tab_icon);

        tabLayout.setViewPager(slidingPanelViewPager);

        tabLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tabLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                LinearLayout content = (LinearLayout) findViewById(R.id.content);
                int topMargin = ((ViewGroup.MarginLayoutParams) content.getLayoutParams()).topMargin;
                bottomSheet.setPeekSheetTranslation(activeUntilLabel.getHeight() + tabLayout.getHeight() + topMargin - 8);
            }
        });

        tabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (bottomSheet.getState().equals(CustomBottomSheetLayout.State.PEEKED)) {
                    bottomSheet.expandSheet();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Floating Action Button
        fab = (FloatingActionButton) bottomSheet.findViewById(R.id.fab_add_users);
        if(isOwner()){
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HaystackActivity.this, UserSelectActivity.class);
                    intent.putExtra(AppConstants.TAG_HAYSTACK, (Parcelable) haystack);
                    startActivityForResult(intent, AppConstants.SELECT_NEW_HAYSTACK_USERS);
                }
            });

            bottomSheet.addOnSheetStateChangeListener(new CustomBottomSheetLayout.OnSheetStateChangeListener() {
                @Override
                public void onSheetStateChanged(CustomBottomSheetLayout.State state) {
                    if (state.equals(CustomBottomSheetLayout.State.PEEKED)) {
                        showFab();
                    } else if (state.equals(CustomBottomSheetLayout.State.EXPANDED)) {
                        hideFab();
                    }
                }
            });
        }else{
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(AppConstants.TAG_HAYSTACK, haystack);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        int menuRes = mIsOwner ? R.menu.haystack_owner : R.menu.haystack;
        getMenuInflater().inflate(menuRes, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstants.SELECT_NEW_HAYSTACK_USERS) {
            if(resultCode == RESULT_OK){
                final ArrayList<UserVO> newUserList = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
                if(newUserList != null){
                    final int count = newUserList.size() - haystack.getUsers().size();

                    HaystackVO updatedHaystack = haystack.clone();
                    updatedHaystack.setUsers(newUserList);

                    ApiClient.getInstance().addUsersToHaystack(updatedHaystack, new Callback<HaystackResult>(){

                        @Override
                        public void onResponse(Call<HaystackResult> call, Response<HaystackResult> response) {
                            HaystackResult result = response.body();
                            if(result.getSuccessCode() == 1){
                                haystack.addUsers(newUserList);

                                HaystackUsersTabFragment fragment = (HaystackUsersTabFragment) pagerAdapter.getFragmentByType(HaystackUsersTabFragment.class);
                                if(fragment != null){
                                    fragment.setHaystack(haystack);
                                }

                                //TODO replace number in string
                                Toast.makeText(HaystackActivity.this, count + " " + getString(R.string.users_added), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(HaystackActivity.this, getString(R.string.error_adding_users), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<HaystackResult> call, Throwable t) {
                            Toast.makeText(HaystackActivity.this, getString(R.string.error_adding_users), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(HaystackActivity.this, getString(R.string.no_users_added), Toast.LENGTH_SHORT).show();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(HaystackActivity.this, getString(R.string.no_users_added), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFab(){
        fab.animate().setDuration(300).setInterpolator(new OvershootInterpolator())
                .scaleY(1.0f).scaleX(1.0f).start();
    }

    private void hideFab(){
        fab.animate().setDuration(300).setInterpolator(new AccelerateInterpolator())
                .scaleY(0.0f).scaleX(0.0f).start();
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
        Needle.networkController.unregister();
        Needle.serviceController.unbindService();

        super.onDestroy();
    }
}
