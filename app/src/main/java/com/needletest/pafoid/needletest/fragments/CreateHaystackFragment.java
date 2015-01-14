package com.needletest.pafoid.needletest.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.activities.HomeActivity;
import com.needletest.pafoid.needletest.activities.MapsActivity;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateHaystackFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateHaystackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateHaystackFragment extends Fragment {

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private static final String CREATE_HAYSTACK_URL = AppConstants.PROJECT_URL +"createHaystack.php";

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private View rootView;
    private OnFragmentInteractionListener mListener;
    private EditText txtName;
    private CheckBox isPublicCheckbox;
    private ToggleButton toggleDateButton;
    private ToggleButton toggleTimeButton;
    private Button changeTimeLimitButton;
    private Button createHaystackButton;
    private TextView timeLimitText;

    private Calendar calendar;
    private String dateLimit;
    private int year, month, day, hours, minutes;
    private String timeLimit;
    private String userName;
    private Haystack haystack;

    public static CreateHaystackFragment newInstance() {
        CreateHaystackFragment fragment = new CreateHaystackFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_create_haystack, container, false);

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

    private void createHaystack(){
        haystack = new Haystack();
        haystack.setName(txtName.getText().toString());
        haystack.setIsPublic(isPublicCheckbox.isChecked());
        if(useDate()){
            haystack.setTimeLimit(dateLimit);
        }else{
            haystack.setTimeLimit(timeLimit);
        }

        haystack.setOwner(getUserName());

        //Users
        ArrayList<String> users = new ArrayList<String>();
        users.add(getUserName());
        haystack.setUsers(users);

        //Active users
        ArrayList<String> activeUsers = new ArrayList<String>();
        activeUsers.add(getUserName());
        haystack.setActiveUsers(activeUsers);

        //Banned users
        ArrayList<String> bannedUsers = new ArrayList<String>();
        haystack.setBannedUsers(bannedUsers);

        new CreateHaystack().execute();
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

    public interface OnFragmentInteractionListener {
    }

    class CreateHaystack extends AsyncTask<Void, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... args) {
            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("name", haystack.getName()));
                params.add(new BasicNameValuePair("owner", haystack.getOwner()));
                params.add(new BasicNameValuePair("isPublic", (haystack.getIsPublic()) ? "1" : "0"));
                params.add(new BasicNameValuePair("timeLimit", haystack.getTimeLimit()));
                params.add(new BasicNameValuePair("zone", haystack.getZone()));
                params.add(new BasicNameValuePair("pictureURL", haystack.getPictureURL()));

                int i;
                ArrayList<String> haystackUsers = haystack.getUsers();
                for(i=0;i<haystackUsers.size();i++){
                    String user = haystackUsers.get(i);
                    params.add(new BasicNameValuePair("haystack_user", user));
                }

                ArrayList<String> haystackActiveUsers = haystack.getActiveUsers();
                for(i=0;i<haystackActiveUsers.size();i++){
                    String user = haystackActiveUsers.get(i);
                    params.add(new BasicNameValuePair("haystack_active_user", user));
                }

                ArrayList<String> haystackBannedUsers = haystack.getBannedUsers();
                for(i=0;i<haystackBannedUsers.size();i++){
                    String user = haystackBannedUsers.get(i);
                    params.add(new BasicNameValuePair("haystack_banned_user", user));
                }

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(CREATE_HAYSTACK_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("CreateHaystack Successful!", json.toString());

                    haystack.setId(json.getInt(AppConstants.TAG_HAYSTACK_ID));

                    Intent intent = new Intent(rootView.getContext(), MapsActivity.class);
                    intent.putExtra("haystack", (Parcelable) haystack);
                    startActivity(intent);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("CreateHaystack Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {

        }

    }

}
