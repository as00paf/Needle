package com.needletest.pafoid.needletest.authentication;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.authentication.task.RegisterTask;
import com.needletest.pafoid.needletest.authentication.task.RegisterTaskParams;

public class RegisterActivity extends Activity implements OnClickListener{
	
	private EditText user, pass;
	private Button  mRegister;
    private String userName, password;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		user = (EditText)findViewById(R.id.username);
		pass = (EditText)findViewById(R.id.password);
        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    register();
                    handled = true;
                }
                return handled;
            }
        });

        user.requestFocus();

        mRegister = (Button)findViewById(R.id.register);
		mRegister.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
        register();
	}

    private void register() {
        if(validateCredentials()){
            RegisterTaskParams params = new RegisterTaskParams(userName, password, this);
            new RegisterTask(params).execute();
        }
    }

    private boolean validateCredentials(){
        userName = user.getText().toString();
        password = pass.getText().toString();

        if(TextUtils.isEmpty(userName))
            return false;
        if(TextUtils.isEmpty(password))
            return false;

        return true;
    }
}
