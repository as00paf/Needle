package com.needletest.pafoid.needletest.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button createHaystackButton;

    private Calendar calendar;
    private String dateLimit;
    private int year, month, day;
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

                datePicker.setVisibility((toggleTimeButton.isChecked()) ? View.INVISIBLE : View.VISIBLE);
                timePicker.setVisibility((toggleDateButton.isChecked()) ? View.INVISIBLE : View.VISIBLE);
            }
        });
        toggleTimeButton = (ToggleButton) rootView.findViewById(R.id.new_haystack_toggleTime);
        toggleTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDateButton.setChecked(!toggleTimeButton.isChecked());

                datePicker.setVisibility((toggleTimeButton.isChecked()) ? View.INVISIBLE : View.VISIBLE);
                timePicker.setVisibility((toggleDateButton.isChecked()) ? View.INVISIBLE : View.VISIBLE);
            }
        });

        datePicker = (DatePicker) rootView.findViewById(R.id.new_haystack_date);
        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //dateLimit = year+"-"+(monthOfYear + 1)+"-"+dayOfMonth;
                SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
                dateLimit = sdf.format(new Date(year - 1900, month, day));
            }
        };

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(rootView.getContext(), datePickerListener, year, month, day).show();
            }
        });

        timePicker = (TimePicker) rootView.findViewById(R.id.new_haystack_time);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATETIME_FORMAT, Locale.US);
                timeLimit = sdf.format(new Date(year, month, day, hourOfDay, minute));
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
                params.add(new BasicNameValuePair("isPublic", haystack.getIsPublic().toString()));
                params.add(new BasicNameValuePair("timeLimit", haystack.getTimeLimit()));

                params.add(new BasicNameValuePair("users", new JSONArray(haystack.getUsers()).toString()));
                params.add(new BasicNameValuePair("activeUsers", new JSONArray(haystack.getActiveUsers()).toString()));
                params.add(new BasicNameValuePair("bannedUsers", new JSONArray(haystack.getBannedUsers()).toString()));

                params.add(new BasicNameValuePair("zone", haystack.getZone()));
                params.add(new BasicNameValuePair("pictureURL", haystack.getPictureURL()));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        CREATE_HAYSTACK_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("CreateHaystack Successful!", json.toString());

                    Intent i = new Intent(rootView.getContext(), MapsActivity.class);
                    i.putExtra("haystack", (Parcelable) haystack);
                    startActivity(i);
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
