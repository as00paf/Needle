package com.nemator.needle.home.createHaystack;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nemator.needle.R;
import com.nemator.needle.haystack.HaystackUserListAdapter;
import com.nemator.needle.utils.AppAnimations;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Alex on 12/04/2015.
 */
public class CreateHaystackGeneralInfosFragment extends CreateHaystackBaseFragment{

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_TIME_FORMAT = "HH:mm";
    public static final String TAG = "CreateHaystackGeneralInfosFragment";

    private View rootView;

    //Children
    private EditText txtName;
    private CheckBox isPublicCheckbox;
    private ImageButton changeDateLimitButton, changeTimeLimitButton;
    private TextView dateLimitText, timeLimitText, usersLabel;
    private Calendar calendar;

    private String dateLimit;
    private int year, month, day, hours, minutes;
    private String timeLimit;

    public static CreateHaystackGeneralInfosFragment newInstance() {
        CreateHaystackGeneralInfosFragment fragment = new CreateHaystackGeneralInfosFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CreateHaystackGeneralInfosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_general, container, false);

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

        return rootView;
    }
}
