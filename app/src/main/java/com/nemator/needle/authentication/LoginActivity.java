package com.nemator.needle.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
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

import com.nemator.needle.R;
import com.nemator.needle.authentication.task.LoginTask;
import com.nemator.needle.authentication.task.LoginTaskParams;
import com.nemator.needle.haystack.HaystackMapFragment;
import com.nemator.needle.haystack.HaystackNavigationDrawerFragment;
import com.nemator.needle.home.HomeActivity;
import com.nemator.needle.AppConstants;
import com.nemator.needle.authentication.task.AuthenticationResult;

public class LoginActivity extends ActionBarActivity implements OnClickListener, LoginTask.LoginResponseHandler, HaystackNavigationDrawerFragment.NavigationDrawerCallbacks {

    private SharedPreferences mSharedPreferences;
    private HaystackNavigationDrawerFragment mNavigationDrawerFragment;
	private EditText user, pass;
	private Button mSubmit, mRegister;
    private CheckBox rememberMeCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

        //Navigation Drawer
        mNavigationDrawerFragment = (HaystackNavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.login_layout));

        //Username & Password
		user = (EditText)findViewById(R.id.usernameEditText);
        user.setText(mSharedPreferences.getString("username", ""));

		pass = (EditText)findViewById(R.id.password);
        pass.setText(mSharedPreferences.getString("password", ""));

        if(!mNavigationDrawerFragment.isDrawerOpen()){
            if(TextUtils.isEmpty(user.getText())){
                user.requestFocus();
            }else{
                pass.requestFocus();
            }
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
        Boolean rememberMe = mSharedPreferences.getBoolean("rememberMe", true);
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
        boolean autoLogin = mSharedPreferences.getBoolean("rememberMe", false);

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
            LoginTaskParams params = new LoginTaskParams(username, password, this, rememberMeCheckBox.isChecked(), false);
            new LoginTask(params, this).execute();
        }
    }

    public void onLoginComplete(AuthenticationResult result){
        if(result.successCode == 1){
            Intent i = new Intent(this, HomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }else{
            Toast.makeText(this, "An Error Occured\n Please Try Again!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position){
            case 0:
                //Haystacks
                break;
            case 1:
                //Settings
                break;
            case 2:
                //Help & Support
                break;
            case 3:

                break;
        }

    }
}
