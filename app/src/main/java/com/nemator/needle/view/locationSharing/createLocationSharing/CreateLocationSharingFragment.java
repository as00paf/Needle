package com.nemator.needle.view.locationSharing.createLocationSharing;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.data.LocationServiceDBHelper;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.service.NeedleLocationService;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingResult;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTask;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTask.CreateLocationSharingResponseHandler;
import com.nemator.needle.tasks.createLocationSharing.CreateLocationSharingTaskParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersParams;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersResult;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersTask;
import com.nemator.needle.tasks.retrieveUsers.RetrieveUsersTask.RetrieveUsersResponseHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateLocationSharingFragment extends Fragment implements RetrieveUsersResponseHandler {
    public static String TAG = "CreateLocationSharingFragment";

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_TIME_FORMAT = "HH:mm";

    private View rootView;

    //Children
    private AutoCompleteTextView friendNameTextField;
    private EditText dateLimitEditText, timeLimitEditText;

    //Data
    private Calendar calendar;
    private String dateLimit;
    private int year, month, day, hours, minutes;
    private String timeLimit;
    private LocationSharingVO locationSharingVO;
    private ArrayList<UserVO> usersList;
    private UserVO selectedUser;
    private FriendAutoCompleteAdapter friendAutoCompleteAdapter;

    public static CreateLocationSharingFragment newInstance() {
        CreateLocationSharingFragment fragment = new CreateLocationSharingFragment();
        return fragment;
    }

    public CreateLocationSharingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_location_sharing, container, false);

        //Friend
        friendNameTextField = (AutoCompleteTextView) rootView.findViewById(R.id.new_location_friend);
        friendNameTextField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = friendAutoCompleteAdapter.getItem(position);
            }
        });

        //Date/Time Limit
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hours = calendar.getTime().getHours()+1;
        minutes = calendar.getTime().getMinutes() + 10;

        SimpleDateFormat sdf = new SimpleDateFormat(SQL_TIME_FORMAT, Locale.US);
        timeLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));

        timeLimitEditText = (EditText) rootView.findViewById(R.id.timeLimitEditText);
        timeLimitEditText.setText(timeLimit);

        timeLimitEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SimpleDateFormat sdf = new SimpleDateFormat(SQL_TIME_FORMAT, Locale.US);
                        timeLimit = sdf.format(new Date(year, month, day, hourOfDay, minute));
                        timeLimitEditText.setText(timeLimit);
                    }
                }, hours, minutes, true);
                dialog.show();
            }
        });

        dateLimitEditText = (EditText) rootView.findViewById(R.id.dateLimitEditText);
        sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
        dateLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));
        dateLimitEditText.setText(dateLimit);
        dateLimitEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
                        dateLimit = sdf.format(new Date(year - 1900, monthOfYear, dayOfMonth));
                        dateLimitEditText.setText(dateLimit);
                    }
                }, year, month, day);

                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();
            }
        });

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchAllUsers();
    }

    //Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_haystack_done, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.create_haystack_action_done) {
            createLocationSharing();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchAllUsers(){
        RetrieveUsersParams params = new RetrieveUsersParams();
        params.userId = String.valueOf(((MainActivity) getActivity()).getUserId());
        params.type = RetrieveUsersParams.RetrieveUsersParamsType.TYPE_ALL_USERS;

        try{
            RetrieveUsersTask task =  new RetrieveUsersTask(params, this);
            task.execute();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    //Response Handlers
    public void onUsersRetrieved(RetrieveUsersResult result){
        usersList = result.userList;
        updateUserList();
    }

    private void updateUserList(){
        friendAutoCompleteAdapter = new FriendAutoCompleteAdapter(getActivity(), R.layout.friend_auto_complete, usersList);
        friendNameTextField.setAdapter(friendAutoCompleteAdapter);

        friendAutoCompleteAdapter.notifyDataSetChanged();
    }

    private void createLocationSharing(){
        if(!validate()) {
            Toast.makeText(getActivity(), "Please select a user from the list", Toast.LENGTH_SHORT).show();
            return;
        }
        locationSharingVO = new LocationSharingVO();

        //Sender
        locationSharingVO.setSenderId(((MainActivity) getActivity()).getUserId());
        locationSharingVO.setSenderName(((MainActivity) getActivity()).getUserName());

        //Receiver
        locationSharingVO.setReceiverId(selectedUser.getUserId());
        locationSharingVO.setReceiverName(selectedUser.getUserName());

        //Options
        locationSharingVO.setTimeLimit(dateLimitEditText.getText().toString() + " " + timeLimitEditText.getText().toString());

        CreateLocationSharingTaskParams params = new CreateLocationSharingTaskParams(getActivity(), locationSharingVO);
        CreateLocationSharingTask task = new CreateLocationSharingTask(params, ((MainActivity) getActivity()));
        task.execute();
    }

    private Boolean validate(){
        if(selectedUser == null) return false;
        return true;
    }
}
