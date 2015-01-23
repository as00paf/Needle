package com.needletest.pafoid.needletest.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.HaystackActivity;
import com.needletest.pafoid.needletest.haystack.HaystackUserActivity;
import com.needletest.pafoid.needletest.haystack.HaystackUserListAdapter;
import com.needletest.pafoid.needletest.home.task.createHaystack.CreateHaystackResult;
import com.needletest.pafoid.needletest.home.task.createHaystack.CreateHaystackTask;
import com.needletest.pafoid.needletest.home.task.createHaystack.CreateHaystackTaskParams;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateHaystackFragment extends Fragment implements CreateHaystackTask.CreateHaystackResponseHandler {
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_TIME_FORMAT = "HH:mm";
    public static final String TAG = "CreateHaystackFragment";

    private View rootView;
    private EditText txtName;
    private CheckBox isPublicCheckbox;
    private ImageButton changeDateLimitButton, changeTimeLimitButton;
    private Button createHaystackButton, addRemoveUsersButton;
    private TextView dateLimitText, timeLimitText;
    private ListView userListView;
    private HaystackUserListAdapter userListAdapter;

    private Calendar calendar;
    private String dateLimit;
    private int year, month, day, hours, minutes;
    private String timeLimit;
    private String userName;
    private Haystack haystack;
    private int userId = -1;
    private ArrayList<User> userList = new ArrayList<User>();

    public static CreateHaystackFragment newInstance() {
        CreateHaystackFragment fragment = new CreateHaystackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack, container, false);

        String title = getResources().getString(R.string.create_haystack);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);

        txtName = (EditText) rootView.findViewById(R.id.new_haystack_name);
        isPublicCheckbox = (CheckBox) rootView.findViewById(R.id.new_haystack_isPublic_checkBox);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hours = calendar.getTime().getHours()+1;
        minutes = calendar.getTime().getMinutes() + 10;

        SimpleDateFormat sdf = new SimpleDateFormat(SQL_TIME_FORMAT, Locale.US);
        timeLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));

        timeLimitText = (TextView) rootView.findViewById(R.id.timeLimitText);
        timeLimitText.setText(timeLimit);

        dateLimitText = (TextView) rootView.findViewById(R.id.dateLimitText);
        sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
        dateLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));
        dateLimitText.setText(dateLimit);

        changeDateLimitButton = (ImageButton) rootView.findViewById(R.id.changeDateLimitButton);
        changeDateLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
                        dateLimit = sdf.format(new Date(year - 1900, monthOfYear, dayOfMonth));
                        dateLimitText.setText(dateLimit);
                    }
                }, year, month, day);

                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();
            }
        });

        changeTimeLimitButton = (ImageButton) rootView.findViewById(R.id.changeTimeLimitButton);
        changeTimeLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SimpleDateFormat sdf = new SimpleDateFormat(SQL_TIME_FORMAT, Locale.US);
                        timeLimit = sdf.format(new Date(year, month, day, hourOfDay, minute));
                        timeLimitText.setText(timeLimit);
                    }
                }, hours, minutes, true);
                dialog.show();
            }
        });

        userListAdapter = new HaystackUserListAdapter(getActionBar().getThemedContext(), R.layout.haystack_drawer_item, userList, null, inflater);

        userListView = (ListView) rootView.findViewById(R.id.haystack_user_list);
        userListView.setAdapter(userListAdapter);

        addRemoveUsersButton = (Button) rootView.findViewById(R.id.add_remove_users_button);
        addRemoveUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRemoveUsers();
            }
        });

        createHaystackButton = (Button) rootView.findViewById(R.id.create_haystack_button);
        createHaystackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHaystack();
            }
        });

        return rootView;
    }

    private void addRemoveUsers(){
        Intent intent = new Intent(getActivity(), HaystackUserActivity.class);
        intent.putParcelableArrayListExtra(AppConstants.TAG_ADDED_USERS, userList);
        intent.putExtra(AppConstants.TAG_REQUEST_CODE, HaystackUserActivity.ADD_REMOVE_USERS);
        startActivityForResult(intent, HaystackUserActivity.ADD_REMOVE_USERS);
    }

    private void createHaystack(){
        haystack = new Haystack();
        haystack.setName(txtName.getText().toString());
        haystack.setIsPublic(isPublicCheckbox.isChecked());
        haystack.setTimeLimit(dateLimit + " " + timeLimit);
        haystack.setOwner(getUserId());

        //Current User
        User user = new User();
        user.setUserId(getUserId());
        user.setUserName(getUserName());

        //Users
        userList.add(user);
        haystack.setUsers(userList);

        //Active users
        ArrayList<User> activeUsers = new ArrayList<User>();
        haystack.setActiveUsers(activeUsers);

        //Banned users
        ArrayList<User> bannedUsers = new ArrayList<User>();
        haystack.setBannedUsers(bannedUsers);

        CreateHaystackTaskParams params = new CreateHaystackTaskParams(rootView.getContext(), haystack);
        try{
            CreateHaystackTask task = new CreateHaystackTask(params, this);
            task.execute();

        }catch (Exception e) {
            Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }
    }

    public void onHaystackCreated(CreateHaystackResult result){
        if(result.successCode == 0){
            Toast.makeText(getActivity(), "An error occured while creating Haystack", Toast.LENGTH_SHORT).show();
        }else{
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(this);
            trans.commit();

            ((HomeActivity) getActivity()).onNavigationDrawerItemSelected(0);

            Toast.makeText(getActivity(), getResources().getString(R.string.haystack_created), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), HaystackActivity.class);
            intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystack);
            startActivity(intent);
        }
    }

    private String getUserName(){
        if(userName == null){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userName = sp.getString("username", null);
        }

        return userName;
    }

    private int getUserId(){
        if(userId == -1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == HaystackUserActivity.ADD_REMOVE_USERS) {
            userList = data.getParcelableArrayListExtra(AppConstants.TAG_USERS);
            updateUserList();
        }
    }

    private void updateUserList(){
        userListAdapter = new HaystackUserListAdapter(getActionBar().getThemedContext(), R.layout.haystack_drawer_item, userList, userList, getLayoutInflater(null));
        userListAdapter.notifyDataSetChanged();

        userListView.setAdapter(userListAdapter);
        userListView.invalidate();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

}
