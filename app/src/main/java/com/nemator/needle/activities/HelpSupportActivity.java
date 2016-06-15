package com.nemator.needle.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.nemator.needle.R;
import com.nemator.needle.adapter.HelpSupportAdapter;
import com.nemator.needle.utils.AppUtils;

public class HelpSupportActivity extends AppCompatActivity implements HelpSupportAdapter.ClickHandler {

    private static final String TAG = "HelpSupportAct";
    private RecyclerView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_help_support);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //List
        listView = (RecyclerView) findViewById(R.id.list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);

        HelpSupportAdapter adapter = new HelpSupportAdapter(this);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickItem(int position) {
        switch (position){
            case HelpSupportAdapter.FAQ:
                startActivity(new Intent(this, FaqActivity.class));
                break;
            case HelpSupportAdapter.HELP:
                break;
            case HelpSupportAdapter.PHONE:
                call();
                break;
            case HelpSupportAdapter.EMAIL:
                sendEmail();
                break;
            case HelpSupportAdapter.WEBSITE:
                visitWebsite();
                break;
            case HelpSupportAdapter.FACEBOOK:
                showFacebookProfile();
                break;
            case HelpSupportAdapter.TWITTER:
                showTwitterProfile();
                break;
            case HelpSupportAdapter.YOUTUBE:
                showYoutubeChannel();
                break;
            case HelpSupportAdapter.DEBUG:
                showDebugDialog();
                break;
        }
    }

    private void showDebugDialog() {
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int verCode = pInfo.versionCode;
        String version = pInfo.versionName + " build " + verCode;

        new AlertDialog.Builder(this).setTitle(R.string.debug).setMessage(getString(R.string.debug_version, version)).create().show();
    }

    private void showFacebookProfile() {
        AppUtils.showFacebookProfile(this, getString(R.string.fb_page));
    }

    private void showTwitterProfile() {
        AppUtils.showTwitterProfile(this, getString(R.string.twitter_user));
    }

    private void showYoutubeChannel() {
        AppUtils.showYoutubeChannel(this, getString(R.string.youtube_channel));
    }

    private void visitWebsite() {
        Uri uri = Uri.parse(getString(R.string.website_url));
        AppUtils.launchWebsite(this, uri);
    }

    private void call() {
        AppUtils.phoneCall(this, "514-550-2723");
    }

    private void sendEmail() {
        String subject = getString(R.string.dev_email_subject);
        String email = getString(R.string.dev_email_address);

        AppUtils.sendEmail(this, email, subject, "");
    }
}
