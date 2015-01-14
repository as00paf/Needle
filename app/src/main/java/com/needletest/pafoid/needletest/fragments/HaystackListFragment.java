package com.needletest.pafoid.needletest.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.needletest.pafoid.needletest.HaystackListAdapter;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.activities.HaystackActivity;
import com.needletest.pafoid.needletest.activities.MapsActivity;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.utils.JSONParser;
import com.shamanland.fab.FloatingActionButton;

import org.apache.http.NameValuePair;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HaystackListFragment extends Fragment {
    private static final String GET_HAYSTACKS_URL = AppConstants.PROJECT_URL +"getHaystacks.php";

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private ListView listView;
    private HaystackListAdapter haystackListAdapter;
    private ArrayList<Object> haystackList = null;
    private ArrayList<Haystack> publicHaystackList = null;
    private ArrayList<Haystack> privateHaystackList = null;
    private ProgressBar progressbar = null;
    FloatingActionButton fab = null;
    JSONParser jsonParser = new JSONParser();

    private String userName;
    private int userId = -1;

    public static HaystackListFragment newInstance(String param1, String param2) {
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
                        .replace(R.id.container, CreateHaystackFragment.newInstance())
                        .commit();
            }
        });
        fab.initBackground();

        listView = (ListView) rootView.findViewById(R.id.haystack_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,	long id) {
                Object o = listView.getItemAtPosition(position);
                Haystack haystackData = (Haystack) o;

                //Intent intent = new Intent(getActivity(), MapsActivity.class);
                Intent intent = new Intent(getActivity(), HaystackActivity.class);
                intent.putExtra("data", (Serializable) haystackData);
                startActivity(intent);
            }
        });

        if(haystackList == null){
            fetchHaystacks();
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            publicHaystackList = savedInstanceState.getParcelableArrayList("publicHaystackList");
            privateHaystackList = savedInstanceState.getParcelableArrayList("privateHaystackList");

            if(publicHaystackList.size() >  0 || privateHaystackList.size() >  0 ){
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
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("publicHaystackList", publicHaystackList);
        outState.putParcelableArrayList("privateHaystackList", privateHaystackList);
    }

    private void fetchHaystacks(){
        new FetchHaystacks().execute(getUserName());
    }

    public void updateHaystackList() {
        haystackListAdapter = new HaystackListAdapter(rootView.getContext());
        listView.setAdapter(haystackListAdapter);
        haystackListAdapter.addAllItems(haystackList);

        haystackListAdapter.notifyDataSetChanged();
        listView.invalidate();

        progressbar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }

    private class FetchHaystacks extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
        }

        @Override
        protected void onPostExecute(Void result) {
            if (null != haystackList) {
                updateHaystackList();
            }else{
                progressbar.setVisibility(View.GONE);
                // There was an error retrieving haystacks
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            int success;
            String username = params[0];

            try {
                // Building Parameters
                List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
                requestParams.add(new BasicNameValuePair("userId", String.valueOf(getUserId())));

                Log.d("request!", "starting with userId : SELECT * FROM haystack INNER JOIN haystack_users ON haystack.id = haystack_users.haystackId AND haystack_users.userId = "+String.valueOf(getUserId())+" WHERE haystack.isPublic = 0");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        GET_HAYSTACKS_URL, "POST", requestParams);

                // check your log for json response
                Log.d("FetchHaystacks attempt", json.toString());

                // json success tag
                success = json.getInt(AppConstants.TAG_SUCCESS);
                Log.d("FetchHaystacks Successful!", json.toString());

                JSONArray publicHaystacks = json.getJSONArray("public_haystacks");
                JSONArray privateHaystacks = json.getJSONArray("private_haystacks");

                if (publicHaystacks != null || privateHaystacks != null) {
                    haystackList = new ArrayList<Object>();
                    publicHaystackList = new ArrayList<Haystack>();
                    privateHaystackList = new ArrayList<Haystack>();


                    //Public haystacks
                    haystackList.add(getResources().getString(R.string.publicHeader));
                    int count = publicHaystacks.length();
                    if(count == 0){
                        haystackList.add(getResources().getString(R.string.noHaystackAvailable));
                    }

                    for (int i = 0; i < count; i++) {
                        JSONObject haystackData = (JSONObject) publicHaystacks.getJSONObject(i);
                        Haystack haystack = new Haystack();

                        haystack.setName(haystackData.getString("name"));
                        haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                        haystack.setOwner(haystackData.getString("owner"));
                        haystack.setTimeLimit(haystackData.getString("timeLimit"));

                       /* haystack.setActiveUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("activeUsers").split(","))));
                        haystack.setUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("users").split(","))));
                        haystack.setBannedUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("bannedUsers").split(","))));*/

                        //Optionals
                        try{
                            String pictureURL = haystackData.getString("pictureURL");
                            if (pictureURL != null)
                                haystack.setPictureURL(pictureURL);
                        }catch(Exception e){
                            Log.e("parseJson", "No pictureURL for #" + i );
                        }

                        try{
                            String zone = haystackData.getString("zoneString");
                            if (zone != null)
                                haystack.setZone(zone);
                        }catch(Exception e){
                            Log.e("parseJson", "No zone for #" + i );
                        }

                        Log.e("parseJson", "Adding Haystack # "+i );
                        publicHaystackList.add(haystack);
                        haystackList.add(haystack);
                    }

                    //Private haystacks
                    haystackList.add(getResources().getString(R.string.privateHeader));
                    count = privateHaystacks.length();
                    if(count == 0){
                        haystackList.add(getResources().getString(R.string.noHaystackAvailable));
                    }

                    for (int i = 0; i < count; i++) {
                        JSONObject haystackData = (JSONObject) privateHaystacks.getJSONObject(i);
                        Haystack haystack = new Haystack();

                        haystack.setName(haystackData.getString("name"));
                        haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                        haystack.setOwner(haystackData.getString("owner"));
                        haystack.setTimeLimit(haystackData.getString("timeLimit"));

                       /* haystack.setActiveUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("activeUsers").split(","))));
                        haystack.setUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("users").split(","))));
                        haystack.setBannedUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("bannedUsers").split(","))));*/

                        //Optionals
                        try{
                            String pictureURL = haystackData.getString("pictureURL");
                            if (pictureURL != null)
                                haystack.setPictureURL(pictureURL);
                        }catch(Exception e){
                            Log.e("parseJson", "No pictureURL for #" + i );
                        }

                        try{
                            String zone = haystackData.getString("zoneString");
                            if (zone != null)
                                haystack.setZone(zone);
                        }catch(Exception e){
                            Log.e("parseJson", "No zone for #" + i );
                        }

                        Log.e("parseJson", "Adding Haystack # "+i );
                        privateHaystackList.add(haystack);
                        haystackList.add(haystack);
                    }
                }else{
                    Log.d("FetchHaystacks Failure!", json.getString(AppConstants.TAG_MESSAGE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
