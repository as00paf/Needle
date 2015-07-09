package com.nemator.needle.tasks.logOut;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Alex on 09/07/2015.
 */
public class LogOutTask extends AsyncTask<Void, Void, Void> {

    private static final String LOGOUT_URL = AppConstants.PROJECT_URL +"logout.php";
    private static final String TAG = "LogoutTask";

    private LogOutResponseHandler delegate;

    private Context context;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog dialog;

    public LogOutTask(Context context, LogOutResponseHandler delegate){
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage("Logging Out ...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... args) {
        int success;
        try {
            JSONObject json = jsonParser.makeHttpRequest(LOGOUT_URL, "GET", new ArrayList<NameValuePair>());

            success = json.getInt(AppConstants.TAG_SUCCESS);

            if (success == 1) {
                Log.d("Log Out Successful!", json.toString());
            }else{
                Log.d("Logout Failure!", json.getString(AppConstants.TAG_MESSAGE));
            }
        } catch (Exception e) {
            Log.d("Logout Failure!", "Error : " + e.getMessage());
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if(dialog!=null) dialog.dismiss();
        delegate.onLogOutComplete();
        super.onPostExecute(aVoid);
    }

    public interface LogOutResponseHandler {
        void onLogOutComplete();
    }
}
