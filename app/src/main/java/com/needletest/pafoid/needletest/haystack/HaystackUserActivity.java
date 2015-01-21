package com.needletest.pafoid.needletest.haystack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.task.RetrieveUsersParams;
import com.needletest.pafoid.needletest.haystack.task.RetrieveUsersResult;
import com.needletest.pafoid.needletest.haystack.task.RetrieveUsersTask;
import com.needletest.pafoid.needletest.home.HaystackListAdapter;
import com.needletest.pafoid.needletest.models.User;

import java.util.ArrayList;

public class HaystackUserActivity extends ActionBarActivity {

    public static final int ADD_REMOVE_USERS = 0;
    public static final int ADD_USERS = 1;
    public static final int REMOVE_USERS = 2;
    public static final int BAN_USERS = 3;

    private ListView listView;
    private Button confirmButton;

    private ArrayList<User> userList = new ArrayList<User>();
    private ArrayList<User> addedUserList = new ArrayList<User>();
    private HaystackUserListAdapter userListAdapter;

    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haystack_user);

        addedUserList = getIntent().getParcelableArrayListExtra("addedUserList");

        listView =  (ListView) findViewById(R.id.userList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        userListAdapter = new HaystackUserListAdapter(this, R.layout.haystack_drawer_item, userList, addedUserList, getLayoutInflater());
        listView.setAdapter(userListAdapter);

        confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        fetchAllUsers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_haystack_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchAllUsers(){
        RetrieveUsersParams params = new RetrieveUsersParams();
        params.userId = String.valueOf(getUserId());

        try{
            RetrieveUsersResult result =  new RetrieveUsersTask(params).execute().get();
            userList = result.userList;
            updateUserList();
        } catch(Exception e){
            e.printStackTrace();
        }

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
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        ArrayList<User> selectedUserList = new ArrayList<User>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                selectedUserList.add(userListAdapter.getItem(position));
        }

        Intent returnIntent = new Intent();
        returnIntent.putParcelableArrayListExtra("users", selectedUserList);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
