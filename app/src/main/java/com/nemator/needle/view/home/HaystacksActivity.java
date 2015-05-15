package com.nemator.needle.view.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;

import com.nemator.needle.MainActivity;
import com.nemator.needle.R;
import com.nemator.needle.view.authentication.LoginFragment;
import com.nemator.needle.view.home.createHaystack.CreateHaystackFragment;
import com.nemator.needle.view.home.createHaystack.HomeActivityState;
import com.nemator.needle.view.home.createHaystack.OnActivityStateChangeListener;
import com.nemator.needle.view.settings.SettingsFragment;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

public class HaystacksActivity extends MaterialNavigationDrawer implements MaterialSectionListener, OnActivityStateChangeListener {
    private SharedPreferences mSharedPreferences;
    private int currentState = HomeActivityState.PUBLIC_HAYSTACK_TAB;

    @Override
    public void init(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Account
        String username = mSharedPreferences.getString("username", "");
        String email = mSharedPreferences.getString("email", "");

        MaterialAccount account = new MaterialAccount(getResources(), username, email, R.drawable.me, R.drawable.mat);
        this.setFirstAccountPhoto(getResources().getDrawable(R.drawable.me));//TODO:Get picture from cache
        this.addAccount(account);

        //Create Sections
        this.addSection(newSection(getString(R.string.title_haystacks), R.drawable.ic_haystack, new HaystackListFragment()));
        this.addSection(newSection(getString(R.string.title_settings), R.drawable.ic_action_settings, new SettingsFragment()));
        this.addSection(newSection(getString(R.string.title_helpAndSupport), R.drawable.ic_action_help, new LoginFragment()));
        this.addDivisor();
        this.addSection(newSection(getString(R.string.title_logOut), R.drawable.ic_action_exit, this));
        this.addDivisor();

        //Navigation Drawer
        Boolean firstNavDrawerLearned = mSharedPreferences.getBoolean("firstNavDrawerLearned", false);

        if(firstNavDrawerLearned){
            this.disableLearningPattern();
        }else{
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("firstNavDrawerLearned", true);
            edit.commit();
        }

        //Back-Stack
        this.setBackPattern(MaterialNavigationDrawer.BACKPATTERN_NONE);
    }

    @Override
    public void onClick(MaterialSection section){
        super.onClick(section);
        if(this.getSectionList().indexOf(section) == 3){
            //Log out
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("autoLogin", false);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        switch (currentState){
            case HomeActivityState.PUBLIC_HAYSTACK_TAB:
                //Goto Login Activity
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("autoLogin", false);
                startActivity(i);
                break;
            case HomeActivityState.PRIVATE_HAYSTACK_TAB:
                //Goto Public tab
                ((HaystackListFragment) getSupportFragmentManager().getFragments().get(0)).goToTab(0);
                break;
            case HomeActivityState.CREATE_HAYSTACK_GENERAL_INFOS:
                //Goto Haystack List
                setFragment(HaystackListFragment.newInstance(), getString(R.string.title_haystacks));
                currentState = HomeActivityState.PUBLIC_HAYSTACK_TAB;
                break;
            case HomeActivityState.CREATE_HAYSTACK_MAP:
                //Goto General Infos
                ((CreateHaystackFragment) getSupportFragmentManager().getFragments().get(0)).goToPage(0);
                break;
            case HomeActivityState.CREATE_HAYSTACK_USERS:
                //Goto Map
                ((CreateHaystackFragment) getSupportFragmentManager().getFragments().get(0)).goToPage(1);
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    @Override
    public void onStateChange(int state) {
        currentState = state;
    }
}
