package com.nemator.needle.view.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nemator.needle.tasks.addUsers.AddUsersTaskParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersTask;
import com.nemator.needle.tasks.TaskResult;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.tasks.addUsers.AddUsersTask;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersResult;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class HaystackUserActivity extends ActionBarActivity implements AddUsersTask.AddUserResponseHandler,
        RetrieveUsersTask.RetrieveUsersResponseHandler {

    private static final String TAG = "HaystackUserActivity";

    public static final int ADD_REMOVE_USERS = 0;
    public static final int ADD_USERS = 1;
    public static final int SELECT_USER_FOR_DIRECTIONS = 2;
    public static final int BAN_USERS = 3;

    private int requestCode;
    private ListView listView;
    private Button confirmButton;

    private ArrayList<UserVO> userList = new ArrayList<UserVO>();
    private ArrayList<UserVO> addedUserList = new ArrayList<UserVO>();
    private HaystackUserListAdapter userListAdapter;

    private int userId = -1;
    private int haystackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haystack_user);

        addedUserList = getIntent().getParcelableArrayListExtra(AppConstants.TAG_ADDED_USERS);

        listView =  (ListView) findViewById(R.id.userList);
        userListAdapter = new HaystackUserListAdapter(this, R.layout.haystack_drawer_item, userList, addedUserList, getLayoutInflater());
        listView.setAdapter(userListAdapter);

        confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        requestCode = getIntent().getIntExtra(AppConstants.TAG_REQUEST_CODE, -1);
        ActionBar actionBar = getSupportActionBar();

        switch(requestCode){
            case ADD_REMOVE_USERS:
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                //actionBar.setTitle(getString(R.string.addRemoveUsers));
                fetchAllUsers();
                break;
            case ADD_USERS:
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                //actionBar.setTitle(getString(R.string.add_users));
                haystackId = getIntent().getIntExtra(AppConstants.TAG_HAYSTACK_ID, -1);
                fetchUsersNotInHaystack(haystackId);
            break;
            case SELECT_USER_FOR_DIRECTIONS:
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
               // actionBar.setTitle(getString(R.string.get_directions_to_user));
                haystackId = getIntent().getIntExtra(AppConstants.TAG_HAYSTACK_ID, -1);
                fetchHaystackActiveUsers(haystackId);
            break;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_haystack_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Actions
    private void fetchAllUsers(){
        RetrieveUsersParams params = new RetrieveUsersParams();
        params.userId = String.valueOf(getUserId());
        params.type = RetrieveUsersParams.RetrieveUsersParamsType.TYPE_ALL_USERS;

        try{
            RetrieveUsersTask task =  new RetrieveUsersTask(params, this);
            task.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void fetchUsersNotInHaystack(int haystackId){
        RetrieveUsersParams params = new RetrieveUsersParams();
        params.userId = String.valueOf(getUserId());
        params.haystackId = haystackId;
        params.type = RetrieveUsersParams.RetrieveUsersParamsType.TYPE_USERS_NOT_IN_HAYSTACK;

        try{
            RetrieveUsersTask task =  new RetrieveUsersTask(params, this);
            task.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void fetchHaystackActiveUsers(int haystackId){
        RetrieveUsersParams params = new RetrieveUsersParams();
        params.userId = String.valueOf(getUserId());
        params.haystackId = haystackId;
        params.type = RetrieveUsersParams.RetrieveUsersParamsType.TYPE_HAYSTACK_ACTIVE_USERS;

        try{
            RetrieveUsersTask task =  new RetrieveUsersTask(params, this);
            task.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onUsersRetrieved(RetrieveUsersResult result){
        userList = result.userList;
        updateUserList();
    }

    private void updateUserList(){
        userListAdapter = new HaystackUserListAdapter(this, R.layout.haystack_drawer_item, userList, addedUserList, getLayoutInflater());
        listView.setAdapter(userListAdapter);

        userListAdapter.notifyDataSetChanged();
        listView.invalidate();
    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    private void confirm(){
        switch(requestCode){
            case ADD_REMOVE_USERS:
                returnSelectedUserList();
                break;
            case ADD_USERS:
                addSelectedUsersToHaystack();
                break;
            case SELECT_USER_FOR_DIRECTIONS:
                returnSelectedUserList();
                break;
        }
    }

    private void addSelectedUsersToHaystack(){
        AddUsersTaskParams params = new AddUsersTaskParams(this, String.valueOf(haystackId), getSelectedUsersList());
        try{
            new AddUsersTask(params, this).execute();
        }catch (Exception e){
            Log.e(TAG, "Error adding users : " + e.getMessage());
            Toast.makeText(this, "An Error Occured Adding Users", Toast.LENGTH_SHORT).show();
        }
    }

    public void onUsersAdded(TaskResult result){
        if(result.successCode == 0){
            Toast.makeText(this, "An Error Occured Adding Users", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Users Successfuly Added", Toast.LENGTH_SHORT).show();
        }

        returnSelectedUserList();
    }

    private ArrayList<UserVO> getSelectedUsersList(){
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<UserVO> selectedUsersList = new ArrayList<UserVO>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                selectedUsersList.add(userListAdapter.getItem(position));
        }

        return selectedUsersList;
    }

    private void returnSelectedUserList(){
        Intent returnIntent = new Intent();
        returnIntent.putParcelableArrayListExtra(AppConstants.TAG_USERS, getSelectedUsersList());
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
