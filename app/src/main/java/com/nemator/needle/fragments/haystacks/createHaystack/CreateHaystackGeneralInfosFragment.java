package com.nemator.needle.fragments.haystacks.createHaystack;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nemator.needle.R;
import com.nemator.needle.activities.CreateHaystackActivity;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.BitmapUtils;
import com.nemator.needle.utils.CameraUtils;
import com.nemator.needle.utils.PermissionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateHaystackGeneralInfosFragment extends CreateHaystackBaseFragment{

    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SQL_TIME_FORMAT = "HH:mm";
    public static final String TAG = "CreateHSGenFragment";

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
    private boolean isCameraShown = false;
    private File captureFile = null;

    private OnPrivacySettingsUpdatedListener privacySettingsCallback;

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
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            privacySettingsCallback = (CreateHaystackActivity) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    private void takePicture(){
        if(PermissionManager.getInstance(getContext()).isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Intent intent = CameraUtils.getImageCaptureIntent();

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                try {
                    captureFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(captureFile));

                startActivityForResult(intent, AppConstants.TAKE_PICTURE);
            }
            isCameraShown = true;
        }else{
            PermissionManager.getInstance(getContext()).requestPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstants.TAKE_PICTURE) {
            isCameraShown = false;
            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                Bitmap bitmap = BitmapFactory.decodeFile(captureFile.getAbsolutePath());

                //Rotate if portrait
                if (bitmap.getHeight() > bitmap.getWidth()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                updatePhoto(bitmap);
            }else{
                Log.d(TAG, "cancelled");
            }
        }
    }

    private void updatePhoto(Bitmap bitmap) {
        mBitmap = bitmap;
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoView.setImageBitmap(mBitmap);
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

    public boolean isCameraShown() {
        return isCameraShown;
    }

    public void setCameraShown(boolean cameraShown) {
        isCameraShown = cameraShown;
    }

    public interface OnPrivacySettingsUpdatedListener{
        void onPrivacySettingsChanged(Boolean isPublic);
    }
}
