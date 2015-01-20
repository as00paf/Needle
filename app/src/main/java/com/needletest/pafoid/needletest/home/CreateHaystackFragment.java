package com.needletest.pafoid.needletest.home;

import android.app.Activity;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.HaystackActivity;
import com.needletest.pafoid.needletest.haystack.HaystackUserActivity;
import com.needletest.pafoid.needletest.haystack.HaystackUserListAdapter;
import com.needletest.pafoid.needletest.home.task.CreateHaystackResult;
import com.needletest.pafoid.needletest.home.task.CreateHaystackTask;
import com.needletest.pafoid.needletest.home.task.CreateHaystackTaskParams;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateHaystackFragment extends Fragment {

    public static final int SELECT_USERS = 1;

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TAG = "CreateHaystackFragment";

    private View rootView;
    private OnFragmentInteractionListener mListener;
    private EditText txtName;
    private CheckBox isPublicCheckbox;
    private ToggleButton toggleDateButton;
    private ToggleButton toggleTimeButton;
    private Button changeTimeLimitButton;
    private Button createHaystackButton;
    private Button addUsersButton;
    private TextView timeLimitText;
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
        toggleDateButton = (ToggleButton) rootView.findViewById(R.id.new_haystack_toggleDate);
        toggleDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimeButton.setChecked(!toggleDateButton.isChecked());
            }
        });
        toggleTimeButton = (ToggleButton) rootView.findViewById(R.id.new_haystack_toggleTime);
        toggleTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDateButton.setChecked(!toggleTimeButton.isChecked());
            }
        });

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hours = calendar.getTime().getHours()+1;
        minutes = calendar.getTime().getMinutes() + 10;

        SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATETIME_FORMAT, Locale.US);
        timeLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));

        timeLimitText = (TextView) rootView.findViewById(R.id.timeLimitText);
        timeLimitText.setText(timeLimit);

        changeTimeLimitButton = (Button) rootView.findViewById(R.id.changeTimeLimitButton);
        changeTimeLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggleTimeButton.isChecked()){
                    TimePickerDialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATETIME_FORMAT, Locale.US);
                            timeLimit = sdf.format(new Date(year, month, day, hourOfDay, minute));
                        }
                    }, hours, minutes, true);
                    dialog.show();
                }else{
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
                            dateLimit = sdf.format(new Date(year - 1900, month, day));
                        }
                    }, year, month, day);
                    dialog.show();
                }
            }
        });

        userListAdapter = new HaystackUserListAdapter(getActionBar().getThemedContext(), R.layout.haystack_drawer_item, userList, inflater);

        userListView = (ListView) rootView.findViewById(R.id.haystack_user_list);
        userListView.setAdapter(userListAdapter);

        addUsersButton = (Button) rootView.findViewById(R.id.add_users_button);
        addUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUsers();
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addUsers(){
        Intent intent = new Intent(getActivity(), HaystackUserActivity.class);
        startActivityForResult(intent, SELECT_USERS);
    }

    private void createHaystack(){
        haystack = new Haystack();
        haystack.setName(txtName.getText().toString());
        haystack.setIsPublic(isPublicCheckbox.isChecked());
        if(useDate()){
            haystack.setTimeLimit(dateLimit);
        }else{
            haystack.setTimeLimit(timeLimit);
        }

        haystack.setOwner(getUserId());

        //Current User
        User user = new User();
        user.setUserId(getUserId());
        user.setUserName(getUserName());

        //Users
        ArrayList<User> users = new ArrayList<User>();
        users.add(user);
        haystack.setUsers(users);

        //Active users
        ArrayList<User> activeUsers = new ArrayList<User>();
        activeUsers.add(user);
        haystack.setActiveUsers(activeUsers);

        //Banned users
        ArrayList<User> bannedUsers = new ArrayList<User>();
        haystack.setBannedUsers(bannedUsers);

        CreateHaystackTaskParams params = new CreateHaystackTaskParams(rootView.getContext(), haystack);
        try{
            CreateHaystackResult result = new CreateHaystackTask(params).execute().get();
            if(result.successCode == 0){

            }else{
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction trans = manager.beginTransaction();
                trans.remove(this);
                trans.commit();

                ((HomeActivity) getActivity()).onNavigationDrawerItemSelected(0);

                Intent intent = new Intent(params.context, HaystackActivity.class);
                intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystack);
                params.context.startActivity(intent);
            }
        }catch (Exception e) {
        }
    }

    private Boolean useDate(){
        return toggleDateButton.isChecked();
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
        if (requestCode == SELECT_USERS) {
            if (resultCode == getActivity().RESULT_OK) {
                userList = data.getParcelableArrayListExtra("users");
                updateUserList();
            }
        }
    }

    private void updateUserList(){
        userListAdapter = new HaystackUserListAdapter(getActionBar().getThemedContext(), R.layout.haystack_drawer_item, userList, getLayoutInflater(null));
        userListAdapter.notifyDataSetChanged();


        userListView.setAdapter(userListAdapter);
        userListView.invalidate();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    public interface OnFragmentInteractionListener {
    }
}
