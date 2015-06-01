package com.nemator.needle.tasks.fetchHaystack;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchHaystacksTask extends AsyncTask<Void, Void, FetchHaystacksResult> {

    private static final String GET_HAYSTACKS_URL = AppConstants.PROJECT_URL +"getHaystacks.php";
    private static final String TAG = "FetchHaystacksTask";

    private FetchHaystackResponseHandler delegate;

    private JSONParser jsonParser = new JSONParser();
    private FetchHaystacksParams params;

    private ArrayList<Object> haystackList = null;
    private ArrayList<HaystackVO> publicHaystackList = null;
    private ArrayList<HaystackVO> privateHaystackList = null;

    public FetchHaystacksTask(FetchHaystacksParams params, FetchHaystackResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        if(params.refreshLayout != null) params.refreshLayout.setRefreshing(true);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(FetchHaystacksResult result) {
        if(params.refreshLayout != null) params.refreshLayout.setRefreshing(false);
        delegate.onHaystackFetched(result);
    }

    @Override
    protected FetchHaystacksResult doInBackground(Void... args) {
        FetchHaystacksResult result = new FetchHaystacksResult();
        int success;

        try {
            //Params
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", params.userId));

            //Request
            JSONObject json = jsonParser.makeHttpRequest(GET_HAYSTACKS_URL, "POST", requestParams);

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            Log.d(TAG, "FetchHaystacks Successful!\n" + json.toString());

            JSONArray publicHaystacks = json.getJSONArray("public_haystacks");
            JSONArray privateHaystacks = json.getJSONArray("private_haystacks");

            if (publicHaystacks != null || privateHaystacks != null) {
                haystackList = new ArrayList<Object>();
                publicHaystackList = new ArrayList<HaystackVO>();
                privateHaystackList = new ArrayList<HaystackVO>();


                //Public haystacks
                int count = publicHaystacks.length();
                for (int i = 0; i < count; i++) {
                    JSONObject haystackData = publicHaystacks.getJSONObject(i);
                    HaystackVO haystack = new HaystackVO();

                    haystack.setId(haystackData.getInt("id"));
                    haystack.setName(haystackData.getString("name"));
                    haystack.setOwner(haystackData.getInt("owner"));
                    haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                    haystack.setZoneRadius(haystackData.getInt("zoneRadius"));
                    haystack.setIsCircle(haystackData.getInt("isCircle") == 1);
                    haystack.setPosition(new LatLng(haystackData.getDouble("lat"), haystackData.getDouble("lng")));
                    haystack.setTimeLimit(haystackData.getString("timeLimit"));

                    ArrayList<UserVO> users = new ArrayList<UserVO>();
                    JSONArray usersData = haystackData.getJSONArray("users");
                    for (int j = 0; j < usersData.length(); j++) {
                        JSONObject userData =  usersData.getJSONObject(j);
                        UserVO user = new UserVO();
                        try{
                            user.setUserId(userData.getInt(AppConstants.TAG_USER_ID));
                            user.setUserName(userData.getString("username"));
                            //user.setPictureURL(userData.getString(AppConstants.TAG_PICTURE_URL));
                        }catch(Exception e){
                            Log.e(TAG, "Error with haystack user" );
                            e.printStackTrace();
                        }

                        users.add(user);
                    }

                    ArrayList<UserVO> activeUsers = new ArrayList<UserVO>();
                    JSONArray activeUsersData = haystackData.getJSONArray("activeUsers");
                    for (int j = 0; j < activeUsersData.length(); j++) {
                        JSONObject activeUserData =  activeUsersData.getJSONObject(j);
                        UserVO activeUser = new UserVO();
                        try{
                            activeUser.setUserId(activeUserData.getInt(AppConstants.TAG_USER_ID));
                            activeUser.setUserName(activeUserData.getString("username"));
                            //activeUser.setPictureURL(activeUserData.getString(AppConstants.TAG_PICTURE_URL));
                        }catch(Exception e){
                            Log.e(TAG, "Error with haystack active user" );
                            e.printStackTrace();
                        }

                        activeUsers.add(activeUser);
                    }

                    haystack.setActiveUsers(activeUsers);
                    haystack.setUsers(users);
                    //haystack.setBannedUsers(new ArrayList<String>(Arrays.asList(haystackData.getString("bannedUsers").split(","))));

                    //Optionals
                    try{
                        String pictureURL = haystackData.getString("pictureURL");
                        if (pictureURL != null)
                            haystack.setPictureURL(pictureURL);
                    }catch(Exception e){
                        Log.e("parseJson", "No pictureURL for #" + i );
                    }

                    Log.e("parseJson", "Adding Haystack # "+i );
                    publicHaystackList.add(haystack);
                    haystackList.add(haystack);
                }

                //Private haystacks
                count = privateHaystacks.length();
                for (int i = 0; i < count; i++) {
                    JSONObject haystackData = privateHaystacks.getJSONObject(i);
                    HaystackVO haystack = new HaystackVO();

                    haystack.setId(haystackData.getInt("id"));
                    haystack.setName(haystackData.getString("name"));
                    haystack.setOwner(haystackData.getInt("owner"));
                    haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                    haystack.setZoneRadius(haystackData.getInt("zoneRadius"));
                    haystack.setIsCircle(haystackData.getInt("isCircle") == 1);
                    haystack.setPosition(new LatLng(haystackData.getDouble("lat"), haystackData.getDouble("lng")));
                    haystack.setTimeLimit(haystackData.getString("timeLimit"));

                    ArrayList<UserVO> users = new ArrayList<UserVO>();
                    JSONArray usersData = haystackData.getJSONArray("users");
                    for (int j = 0; j < usersData.length(); j++) {
                        JSONObject userData =  usersData.getJSONObject(j);
                        UserVO user = new UserVO();
                        try{
                            user.setUserId(userData.getInt(AppConstants.TAG_USER_ID));
                            user.setUserName(userData.getString("username"));
                            //user.setPictureURL(userData.getString(AppConstants.TAG_PICTURE_URL));
                        }catch(Exception e){
                            Log.e(TAG, "Error with haystack user" );
                            e.printStackTrace();
                        }

                        users.add(user);
                    }

                    ArrayList<UserVO> activeUsers = new ArrayList<UserVO>();
                    JSONArray activeUsersData = (JSONArray) haystackData.getJSONArray("activeUsers");
                    for (int j = 0; j < activeUsersData.length(); j++) {
                        JSONObject activeUserData =  (JSONObject) activeUsersData.getJSONObject(j);
                        UserVO activeUser = new UserVO();
                        try{
                            activeUser.setUserId(activeUserData.getInt(AppConstants.TAG_USER_ID));
                            activeUser.setUserName(activeUserData.getString("username"));
                            //activeUser.setPictureURL(activeUserData.getString(AppConstants.TAG_PICTURE_URL));
                        }catch(Exception e){
                            Log.e(TAG, "Error with haystack active user" );
                            e.printStackTrace();
                        }

                        activeUsers.add(activeUser);
                    }

                    haystack.setActiveUsers(activeUsers);
                    haystack.setUsers(users);

                    //Optionals
                    try{
                        String pictureURL = haystackData.getString("pictureURL");
                        if (pictureURL != null)
                            haystack.setPictureURL(pictureURL);
                    }catch(Exception e){
                        Log.e("parseJson", "No pictureURL for #" + i );
                    }

                    Log.e("parseJson", "Adding Haystack # "+i );
                    privateHaystackList.add(haystack);
                    haystackList.add(haystack);
                }

                result.publicHaystackList = publicHaystackList;
                result.privateHaystackList = privateHaystackList;
                result.haystackList = haystackList;

                return result;
            }else{
                Log.d("FetchHaystacks Failure!", json.getString(AppConstants.TAG_MESSAGE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public interface FetchHaystackResponseHandler {
        void onHaystackFetched(FetchHaystacksResult result);
    }
}
