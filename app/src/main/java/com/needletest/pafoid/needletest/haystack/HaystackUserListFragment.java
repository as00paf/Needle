package com.needletest.pafoid.needletest.haystack;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.models.User;

import java.util.ArrayList;

public class HaystackUserListFragment extends Fragment {
    private View rootView;
    private ListView listView;
    private HaystackUserListAdapter listAdapter;
    private ArrayList<User> userList;

    public Haystack haystack;

    public static HaystackUserListFragment newInstance(Haystack haystack) {
        HaystackUserListFragment fragment = new HaystackUserListFragment();
        Bundle args = new Bundle();
        args.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        fragment.setArguments(args);

        fragment.haystack = haystack;

        return fragment;
    }

    public HaystackUserListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(AppConstants.HAYSTACK_DATA_KEY) && this.haystack == null){
                this.haystack = savedInstanceState.getParcelable(AppConstants.HAYSTACK_DATA_KEY);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(AppConstants.HAYSTACK_DATA_KEY, haystack);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_haystack_user_list, container, false);

        //Populate list
        listView = (ListView) rootView.findViewById(R.id.haystackUserList);
        userList = haystack.getUsers();
        if(userList != null){
            listAdapter = new HaystackUserListAdapter(getActionBar().getThemedContext(), R.layout.haystack_drawer_item, userList, getSelectedUsers(), inflater);
            listView.setAdapter(listAdapter);
        }

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private ArrayList<User> getSelectedUsers(){
        ArrayList<User> result = new ArrayList<User>();
        if(listAdapter != null){
            for(int i = 0;i<listAdapter.getCount();i++){
                if(listAdapter.getView(i, null, null).isSelected()){
                    result.add(listAdapter.getItem(i));
                }
            }
        }


        return result;
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }
}
