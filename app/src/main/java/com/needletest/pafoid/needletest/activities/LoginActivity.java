package com.needletest.pafoid.needletest.activities;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.utils.JSONParser;

public class LoginActivity extends Activity implements OnClickListener{
	
	private EditText user, pass;
	private Button mSubmit, mRegister;
    private CheckBox rememberMeCheckBox;

	 // Progress Dialog
    private ProgressDialog pDialog;
 
    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private static final String LOGIN_URL = AppConstants.PROJECT_URL +"login.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(LoginActivity.this);

		user = (EditText)findViewById(R.id.username);
        user.setText(sp.getString("username", ""));
		pass = (EditText)findViewById(R.id.password);
        pass.setText(sp.getString("password", ""));

        if(TextUtils.isEmpty(user.getText())){
            user.requestFocus();
        }else{
            pass.requestFocus();
        }

        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    login();
                    handled = true;
                }
                return handled;
            }
        });

        //Remember me CheckBox
        Boolean rememberMe = sp.getBoolean("rememberMe", true);
        rememberMeCheckBox = (CheckBox) findViewById(R.id.rememberMeCheckBox);
        rememberMeCheckBox.setChecked(rememberMe);

		//setup buttons
		mSubmit = (Button)findViewById(R.id.login);
		mRegister = (Button)findViewById(R.id.register);
		
		//register listeners
		mSubmit.setOnClickListener(this);
		mRegister.setOnClickListener(this);
		
	}

    @Override
    public void onResume(){
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(LoginActivity.this);
        boolean autoLogin = sp.getBoolean("rememberMe", false);

        if(!user.getText().toString().isEmpty() && !pass.getText().toString().isEmpty() && autoLogin){
            login();
        }

        super.onResume();
    }

    private void login(){
        Log.i(AppConstants.TAG_MESSAGE, "Trying to login with credentials : "+user.getText().toString() +", "+pass.getText().toString());

        String username = user.getText().toString();
        String password = pass.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "You must enter a username and a password", Toast.LENGTH_LONG).show();
        }else{
            new AttemptLogin().execute();
        }
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.login:
               login();
			break;
		case R.id.register:
				Intent i = new Intent(this, RegisterActivity.class);
				startActivity(i);
			break;

		default:
			break;
		}
	}
	
	class AttemptLogin extends AsyncTask<String, String, String> {

		 /**
         * Before starting background thread Show Progress Dialog
         * */
		boolean failure = false;
		
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
		
		@Override
		protected String doInBackground(String... args) {
			 // Check for success tag
            int success;
            String username = user.getText().toString();
            String password = pass.getText().toString();
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
 
                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                       LOGIN_URL, "POST", params);
 
                // check your log for json response
                Log.d("Login attempt", json.toString());
 
                // json success tag
                success = json.getInt(AppConstants.TAG_SUCCESS);
                if (success == 1) {
                	Log.d("Login Successful!", json.toString());

                    // save user data
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(LoginActivity.this);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("username", username);
                    edit.putInt("userId", json.getInt(AppConstants.TAG_USER_ID));
                    edit.putBoolean("rememberMe", rememberMeCheckBox.isChecked());
                    if(rememberMeCheckBox.isChecked()){
                        edit.putString("password", password);
                    }

                    edit.commit();

                	//Intent i = new Intent(Login.this, MapsActivity.class);
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                	finish();
    				startActivity(i);
                	return json.getString(AppConstants.TAG_MESSAGE);
                }else{
                	Log.d("Login Failure!", json.getString(AppConstants.TAG_MESSAGE));
                	return json.getString(AppConstants.TAG_MESSAGE);
                	
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
			
		}
		/**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null){
            	Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
 
        }
		
	}
		 

}
