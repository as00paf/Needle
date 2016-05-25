package com.nemator.needle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nemator.needle.R;
import com.nemator.needle.adapter.UserCardAdapter;
import com.nemator.needle.api.ApiClient;
import com.nemator.needle.api.result.UsersResult;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppConstants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSelectActivity extends AppCompatActivity{

    public static final String TAG = "UserSelectActivity";

    private Toolbar toolbar;
    private RecyclerView listView;
    private SwipeRefreshLayout swipeLayout;

    private UserCardAdapter userListAdapter;
    private GridLayoutManager layoutManager;
    private HaystackVO haystack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_users);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.select_users_to_add));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //List
        listView =  (RecyclerView) findViewById(R.id.list_view);
        int spanCount = 2;//TODO : define span depending on screen width
        layoutManager = new GridLayoutManager(this, spanCount);
        listView.setLayoutManager(layoutManager);

        //Swipe Layout
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.select_users_swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUsersNotInHaystack();
            }
        });
        swipeLayout.setRefreshing(true);

        Bundle bundle = getIntent().getExtras();
        haystack = bundle.getParcelable(AppConstants.TAG_HAYSTACK);
        if(haystack != null){
            fetchUsersNotInHaystack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_done:
            case android.R.id.home:
                returnSelectedUsers();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchUsersNotInHaystack() {
        ApiClient.getInstance().fetchUsersNotInHaystack(haystack.getId(), usersNotInHaystackFetchedCallback);
    }

    private Callback<UsersResult> usersNotInHaystackFetchedCallback = new Callback<UsersResult>() {
        @Override
        public void onResponse(Call<UsersResult> call, Response<UsersResult> response) {
            swipeLayout.setRefreshing(false);

            UsersResult result = response.body();
            if(result.getSuccessCode() == 1){
                userListAdapter = new UserCardAdapter(UserSelectActivity.this, result.getUsers(), UserCardAdapter.Type.MULTI_SELECT);
                listView.setAdapter(userListAdapter);
            }else{
                Toast.makeText(UserSelectActivity.this, getString(R.string.fetching_users_failed), Toast.LENGTH_SHORT).show();
                Log.d(TAG, getString(R.string.fetching_users_failed) + result.getMessage());
            }
        }

        @Override
        public void onFailure(Call<UsersResult> call, Throwable t) {
            swipeLayout.setRefreshing(false);

            Toast.makeText(UserSelectActivity.this, getString(R.string.fetching_users_failed), Toast.LENGTH_SHORT).show();
            Log.d(TAG, getString(R.string.fetching_users_failed) + t.getMessage());
        }
    };

    private void returnSelectedUsers(){
        Intent returnIntent = new Intent();
        if(userListAdapter != null){
            returnIntent.putExtra(AppConstants.TAG_USERS, userListAdapter.getSelectedItems());
        }
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void cancel(){
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}
