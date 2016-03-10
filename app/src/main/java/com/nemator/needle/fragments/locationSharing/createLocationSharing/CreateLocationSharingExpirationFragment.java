package com.nemator.needle.fragments.locationSharing.createLocationSharing;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TimePicker;

import com.nemator.needle.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateLocationSharingExpirationFragment extends Fragment {
    public static String TAG = "CreateLocationSharingExpirationFragment";

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_TIME_FORMAT = "HH:mm";

    private View rootView;

    //Children
    private EditText timeLimitEditText;
    private CalendarView calendarView;

    //Data
    private Calendar calendar;
    private String dateLimit;
    private int year, month, day, hours, minutes;
    private String timeLimit;

    private SimpleDateFormat sdf;

    public static CreateLocationSharingExpirationFragment newInstance() {
        CreateLocationSharingExpirationFragment fragment = new CreateLocationSharingExpirationFragment();
        return fragment;
    }

    public CreateLocationSharingExpirationFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_create_location_sharing_expiration, container, false);


        sdf = new SimpleDateFormat(SQL_TIME_FORMAT, Locale.US);

        //Date/Time Limit
        calendarView = (CalendarView) rootView.findViewById(R.id.calendar);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hours = calendar.getTime().getHours()+1;
        minutes = calendar.getTime().getMinutes() + 10;

        //calendarView.setDate(calendar.get(Calendar.DATE));
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                dateLimit = sdf.format(new Date(year - 1900, month, dayOfMonth));
            }
        });

        timeLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));

        timeLimitEditText = (EditText) rootView.findViewById(R.id.timeLimitEditText);
        timeLimitEditText.setText(timeLimit);

        timeLimitEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        timeLimit = sdf.format(new Date(year, month, day, hourOfDay, minute));
                        timeLimitEditText.setText(timeLimit);
                    }
                }, hours, minutes, true);
                dialog.show();
            }
        });

        sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.US);
        dateLimit = sdf.format(new Date(year-1900, month, day, hours, minutes));
        return rootView;
    }

    public String getDateLimit() {
        return dateLimit;
    }

    public String getTimeLimit() {
        return timeLimit;
    }
}
