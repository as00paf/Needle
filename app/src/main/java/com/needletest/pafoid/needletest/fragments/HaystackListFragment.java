package com.needletest.pafoid.needletest.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.needletest.pafoid.needletest.AppConstants;
import com.needletest.pafoid.needletest.HaystackListAdapter;
import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.activities.HomeActivity;
import com.needletest.pafoid.needletest.activities.MapsActivity;
import com.needletest.pafoid.needletest.activities.RegisterActivity;
import com.needletest.pafoid.needletest.models.Haystack;
import com.needletest.pafoid.needletest.utils.JSONParser;
import com.shamanland.fab.FloatingActionButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HaystackListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HaystackListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HaystackListFragment extends Fragment {

    private static final String GET_HAYSTACKS_URL = AppConstants.PROJECT_URL +"getHaystacks.php";
    private static final int MAX_HAYSTACK_COUNT =20;

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private ListView listView;
    private HaystackListAdapter haystackListAdapter;
    private ArrayList<Haystack> haystackList = null;
    private int haystackCount = 0;
    private ProgressBar progressbar = null;
    FloatingActionButton fab = null;
    JSONParser jsonParser = new JSONParser();
    //JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private String userName;

    public static HaystackListFragment newInstance(String param1, String param2) {
        HaystackListFragment fragment = new HaystackListFragment();
        return fragment;
    }

    public HaystackListFragment() {
        // Required empty public constructor
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

        haystackListAdapter = new HaystackListAdapter(rootView.getContext(), haystackList);
        listView = (ListView) rootView.findViewById(R.id.haystack_list);
        listView.setAdapter(haystackListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,	long id) {
                Object o = listView.getItemAtPosition(position);
                Haystack haystackData = (Haystack) o;

                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("data", (Serializable) haystackData);
                startActivity(intent);
            }
        });

        if(haystackList == null){
            fetchHaystacks();
        }

        return rootView;
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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            haystackList = savedInstanceState.getParcelableArrayList("haystackList");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("haystackList", haystackList);
    }

    private void fetchHaystacks(){
        new FetchHaystacks().execute(getUserName());
    }

    public void updateHaystackList() {
        haystackListAdapter = new HaystackListAdapter(rootView.getContext(), haystackList);
        listView.setAdapter(haystackListAdapter);

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
                requestParams.add(new BasicNameValuePair("username", username));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        GET_HAYSTACKS_URL, "POST", requestParams);

                // check your log for json response
                Log.d("FetchHaystacks attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("FetchHaystacks Successful!", json.toString());

                    JSONArray haystacks = json.getJSONArray("haystacks");
                    if (haystacks != null) {
                        haystackList = new ArrayList<Haystack>();
                        int count = haystacks.length();

                        for (int i = 0; i < count; i++) {
                            JSONObject haystackData = (JSONObject) haystacks.getJSONObject(i);
                            Haystack haystack = new Haystack();

                            haystack.setName(haystackData.getString("name"));
                            haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                            haystack.setOwner(haystackData.getString("owner"));
                            haystack.setTimeLimit(haystackData.getString("timeLimit"));

                            haystack.setActiveUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("activeUsers").split(","))));
                            haystack.setUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("users").split(","))));
                            haystack.setBannedUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("bannedUsers").split(","))));

                            //Optionals
                            try{
                                String pictureURL = haystackData.getString("pictureURL");
                                if (pictureURL != null)
                                    haystack.setPictureURL(pictureURL);
                            }catch(Exception e){
                                Log.e("parseJson", "No pictureURL for #" + i );
                            }

                            try{
                                String zone = haystackData.getString("zone");
                                if (zone != null)
                                    haystack.setZone(zone);
                            }catch(Exception e){
                                Log.e("parseJson", "No zone for #" + i );
                            }

                            Log.e("parseJson", "Adding Haystack # "+i );
                            haystackList.add(haystack);
                        }
                    }
                }else{
                    Log.d("FetchHaystacks Failure!", json.getString(TAG_MESSAGE));
                    haystackList = null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
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
