package com.nemator.needle.view.haystacks.createHaystack;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.GoogleApiClient;
import com.nemator.needle.R;

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

    //Children
    private EditText txtName, dateLimitEditText, timeLimitEditText;
    private SwitchCompat isPublicSwitch;
    private TextView privacyLabel;
    private Calendar calendar;

    private String dateLimit;
    private int year, month, day, hours, minutes;
    private String timeLimit;
    private ImageView photoView;
    private Bitmap mBitmap;

    private OnPrivacySettingsUpdatedListener privacySettingsCallback;
    private GoogleApiClient mGoogleApiClient;

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
        connectToApiClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_haystack_general, container, false);

        //Name
        txtName = (EditText) rootView.findViewById(R.id.new_haystack_name);

        //Privacy
        isPublicSwitch = (SwitchCompat) rootView.findViewById(R.id.new_haystack_isPublic_switch);
        isPublicSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    privacyLabel.setText(getString(R.string.isPublic));
                    privacyLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
                }else{
                    privacyLabel.setText(getString(R.string.privateLabel));
                    privacyLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_outline_black_24dp, 0, 0, 0);
                }

                //Remove/Restore Users Fragment
                privacySettingsCallback.onPrivacySettingsChanged(isChecked);
            }
        });

        privacyLabel = (TextView) rootView.findViewById(R.id.new_haystack_privacy_label);

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

        photoView = (ImageView) rootView.findViewById(R.id.new_haystack_photo);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            privacySettingsCallback = (CreateHaystackFragment) getFragmentManager().getFragments().get(0);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    public void updatePhoto(Bitmap bitmap) {
        mBitmap = bitmap;
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setImageBitmap(mBitmap);
    }

    private void connectToApiClient(){
        if(mGoogleApiClient == null){
            mGoogleApiClient = ((CreateHaystackFragment) getParentFragment()).getGoogleApiClient();
        }
    }

    public String getHaystackName(){
        return txtName.getEditableText().toString();
    }

    public Boolean getIsPublic(){
        return isPublicSwitch.isChecked();
    }

    public String getDateLimit(){
        return dateLimitEditText.getText().toString();
    }

    public String getTimeLimit(){
        return timeLimitEditText.getText().toString();
    }

    public Bitmap getPicture(){
        return mBitmap;
    }

    public interface OnPrivacySettingsUpdatedListener{
        void onPrivacySettingsChanged(Boolean isPublic);
    }
}
