package com.needletest.pafoid.needletest.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.HaystackActivity;
import com.needletest.pafoid.needletest.home.task.fetchHaystack.FetchHaystacksParams;
import com.needletest.pafoid.needletest.home.task.fetchHaystack.FetchHaystacksResult;
import com.needletest.pafoid.needletest.home.task.fetchHaystack.FetchHaystacksTask;
import com.needletest.pafoid.needletest.models.Haystack;

import com.shamanland.fab.FloatingActionButton;

import java.util.ArrayList;

public class HaystackListFragment extends Fragment implements FetchHaystacksTask.FetchHaystackResponseHandler{
    private static final String TAG = "HaystackListFragment";

    private View rootView;
    private ListView listView;
    private HaystackListAdapter haystackListAdapter;
    private ArrayList<Object> haystackList = null;
    private ArrayList<Haystack> publicHaystackList = null;
    private ArrayList<Haystack> privateHaystackList = null;
    private ProgressBar progressbar = null;
    FloatingActionButton fab = null;

    private String userName;
    private int userId = -1;

    public static HaystackListFragment newInstance() {
        HaystackListFragment fragment = new HaystackListFragment();
        return fragment;
    }

    public HaystackListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_haystack_list, container, false);
        progressbar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setColor(getResources().getColor(R.color.primary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.home_fragment_container, CreateHaystackFragment.newInstance())
                        .commit();
            }
        });
        fab.initBackground();

        listView = (ListView) rootView.findViewById(R.id.haystack_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,	long id) {
                Object o = listView.getItemAtPosition(position);
                if(o instanceof String){
                    //Header
                    return;
                }

                Haystack haystackData = (Haystack) o;
                Intent intent = new Intent(getActivity(), HaystackActivity.class);
                intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystackData);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            publicHaystackList = savedInstanceState.getParcelableArrayList("publicHaystackList");
            privateHaystackList = savedInstanceState.getParcelableArrayList("privateHaystackList");

            if(haystackList == null && (publicHaystackList.size() >  0 || privateHaystackList.size() >  0 )){
                haystackList = new ArrayList<Object>();
                haystackList.add(getResources().getString(R.string.publicHeader));

                int i;
                int count = publicHaystackList.size();
                if(count==0){
                    haystackList.add(getResources().getString(R.string.noHaystackAvailable));
                }

                for(i=0;i<count;i++){
                    haystackList.add(publicHaystackList.get(i));
                }

                haystackList.add(getResources().getString(R.string.privateHeader));
                count = privateHaystackList.size();
                if(count==0){
                    haystackList.add(getResources().getString(R.string.noHaystackAvailable));
                }

                for(i=0;i<count;i++){
                    haystackList.add(privateHaystackList.get(i));
                }
            }
        }else {
            updateHaystackList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("publicHaystackList", publicHaystackList);
        outState.putParcelableArrayList("privateHaystackList", privateHaystackList);
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchHaystacks();
    }

    public void fetchHaystacks(){
        FetchHaystacksParams params = new FetchHaystacksParams(getUserName(), String.valueOf(getUserId()), rootView.getContext(), progressbar);

        try{
            FetchHaystacksTask task = new FetchHaystacksTask(params, this);
            task.execute();
        }catch(Exception e){
            Log.e(TAG, "fetchHaystacks exception : "+e.toString());
        }
    }

    public void onHaystackFetched(FetchHaystacksResult result){
        haystackList = result.haystackList;
        publicHaystackList = result.publicHaystackList;
        privateHaystackList = result.privateHaystackList;

        updateHaystackList();
    }


    public void updateHaystackList() {
        if(rootView == null || haystackList == null){
            return;
        }

        haystackListAdapter = new HaystackListAdapter(rootView.getContext());
        listView.setAdapter(haystackListAdapter);
        haystackListAdapter.addAllItems(haystackList);

        haystackListAdapter.notifyDataSetChanged();
        listView.invalidate();

        progressbar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    private int getUserId(){
        if(userId==-1){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userId = sp.getInt("userId", -1);
        }

        return userId;
    }

    private String getUserName(){
        if(userName == null){
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(rootView.getContext());

            userName = sp.getString("username", null);
        }

        return userName;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
