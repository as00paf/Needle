package com.nemator.needle.home.task.fetchHaystack;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.nemator.needle.models.Haystack;
import com.nemator.needle.AppConstants;
import com.nemator.needle.R;
import com.nemator.needle.models.User;
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
    private ArrayList<Haystack> publicHaystackList = null;
    private ArrayList<Haystack> privateHaystackList = null;

    public FetchHaystacksTask(FetchHaystacksParams params, FetchHaystackResponseHandler delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute(FetchHaystacksResult result) {
        params.progressbar.setVisibility(View.GONE);
        delegate.onHaystackFetched(result);
    }

    @Override
    protected FetchHaystacksResult doInBackground(Void... args) {
        FetchHaystacksResult result = new FetchHaystacksResult();
        int success;

        try {
            List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
            requestParams.add(new BasicNameValuePair("userId", params.userId));

            Log.d(TAG, "starting with userId : SELECT * FROM haystack INNER JOIN haystack_users ON haystack.id = haystack_users.haystackId AND haystack_users.userId = " + params.userId + " WHERE haystack.isPublic = 0");

            JSONObject json = jsonParser.makeHttpRequest(GET_HAYSTACKS_URL, "POST", requestParams);

            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;

            Log.d("FetchHaystacks Successful!", json.toString());

            JSONArray publicHaystacks = json.getJSONArray("public_haystacks");
            JSONArray privateHaystacks = json.getJSONArray("private_haystacks");

            if (publicHaystacks != null || privateHaystacks != null) {
                haystackList = new ArrayList<Object>();
                publicHaystackList = new ArrayList<Haystack>();
                privateHaystackList = new ArrayList<Haystack>();


                //Public haystacks
                haystackList.add(params.context.getResources().getString(R.string.publicHeader));
                int count = publicHaystacks.length();
                if(count == 0){
                    haystackList.add(params.context.getResources().getString(R.string.noHaystackAvailable));
                }

                for (int i = 0; i < count; i++) {
                    JSONObject haystackData = (JSONObject) publicHaystacks.getJSONObject(i);
                    Haystack haystack = new Haystack();

                    haystack.setId(haystackData.getInt("id"));
                    haystack.setName(haystackData.getString("name"));
                    haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                    haystack.setOwner(haystackData.getInt("owner"));
                    haystack.setTimeLimit(haystackData.getString("timeLimit"));

                    ArrayList<User> users = new ArrayList<User>();
                    JSONArray usersData = (JSONArray) haystackData.getJSONArray("users");
                    for (int j = 0; j < usersData.length(); j++) {
                        JSONObject userData =  (JSONObject) usersData.getJSONObject(j);
                        User user = new User();
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

                    ArrayList<User> activeUsers = new ArrayList<User>();
                    JSONArray activeUsersData = (JSONArray) haystackData.getJSONArray("activeUsers");
                    for (int j = 0; j < activeUsersData.length(); j++) {
                        JSONObject activeUserData =  (JSONObject) activeUsersData.getJSONObject(j);
                        User activeUser = new User();
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
                haystackList.add(params.context.getResources().getString(R.string.privateHeader));
                count = privateHaystacks.length();
                if(count == 0){
                    haystackList.add(params.context.getResources().getString(R.string.noHaystackAvailable));
                }

                for (int i = 0; i < count; i++) {
                    JSONObject haystackData = (JSONObject) privateHaystacks.getJSONObject(i);
                    Haystack haystack = new Haystack();

                    haystack.setId(haystackData.getInt("id"));
                    haystack.setName(haystackData.getString("name"));
                    haystack.setIsPublic(haystackData.getInt("isPublic") == 1);
                    haystack.setOwner(haystackData.getInt("owner"));
                    haystack.setTimeLimit(haystackData.getString("timeLimit"));

                    ArrayList<User> users = new ArrayList<User>();
                    JSONArray usersData = (JSONArray) haystackData.getJSONArray("users");
                    for (int j = 0; j < usersData.length(); j++) {
                        JSONObject userData =  (JSONObject) usersData.getJSONObject(j);
                        User user = new User();
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

                    ArrayList<User> activeUsers = new ArrayList<User>();
                    JSONArray activeUsersData = (JSONArray) haystackData.getJSONArray("activeUsers");
                    for (int j = 0; j < activeUsersData.length(); j++) {
                        JSONObject activeUserData =  (JSONObject) activeUsersData.getJSONObject(j);
                        User activeUser = new User();
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
