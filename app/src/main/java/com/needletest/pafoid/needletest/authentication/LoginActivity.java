package com.needletest.pafoid.needletest.authentication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.needletest.pafoid.needletest.authentication.task.LoginTask;
import com.needletest.pafoid.needletest.authentication.task.LoginTaskParams;

public class LoginActivity extends Activity implements OnClickListener{
	
	private EditText user, pass;
	private Button mSubmit, mRegister;
    private CheckBox rememberMeCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

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
            LoginTaskParams params = new LoginTaskParams(username, password, this, rememberMeCheckBox.isChecked());
            new LoginTask(params).execute();
        }
    }

	@Override
	public void onClick(View v) {
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
}
