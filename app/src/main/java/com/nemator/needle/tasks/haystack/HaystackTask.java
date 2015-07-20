package com.nemator.needle.tasks.haystack;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.nemator.needle.R;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.locationSharing.LocationSharingParams;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HaystackTask extends AsyncTask<Void, Void, HaystackTaskResult> {

    private static final String LOCATION_SHARING_URL = AppConstants.PROJECT_URL +"haystack.php";
    private static final String TAG = "HaystackTask";

    private Object delegate;

    private JSONParser jsonParser = new JSONParser();
    private HaystackTaskParams params;
    private ProgressDialog dialog;

    public HaystackTask(HaystackTaskParams params, Object delegate){
        this.params = params;
        this.delegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        String message = null;
        Boolean showDialog = false;
        Boolean isCancellable = true;
        switch(params.type){
            case HaystackTaskParams.TYPE_GET :
                break;
            case HaystackTaskParams.TYPE_CREATE :
                message = params.context.getResources().getString(R.string.creating_haystack);
                isCancellable = false;
                break;
            case HaystackTaskParams.TYPE_UPDATE :
                //message = params.context.getResources().getString(R.string.sharing_location);
                break;
            case HaystackTaskParams.TYPE_DELETE :
                //message = params.context.getResources().getString(R.string.cancelling_location_sharing);
                break;
            default:
                message = "Haystack";
        }

        if(showDialog && message != null){
            dialog = new ProgressDialog(params.context);
            dialog.setMessage(message);
            dialog.setIndeterminate(false);
            dialog.setCancelable(isCancellable);
            dialog.show();
        }


        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(HaystackTaskResult result) {
        switch(params.type){
            case HaystackTaskParams.TYPE_GET :
                ((FetchHaystackResponseHandler) delegate).onHaystackFetched(result);
                break;
            case HaystackTaskParams.TYPE_CREATE :
                ((CreateHaystackResponseHandler) delegate).onHaystackCreated(result);
                break;
            /*case HaystackTaskParams.TYPE_UPDATE :
                ((UpdateLocationSharingTaskHandler) delegate).onLocationSharingUpdated(result);
                break;
            case HaystackTaskParams.TYPE_DELETE :
                ((CancelLocationSharingResponseHandler) delegate).onLocationSharingCancelled(result);
                break;*/
        }

        if(dialog !=null){
            dialog.dismiss();
        }
    }

    @Override
    protected HaystackTaskResult doInBackground(Void... args) {
        Log.i(TAG, "Sending Haystack request of type " + params.type);

        HaystackTaskResult result = new HaystackTaskResult();
        int success;
        String message = "not set";

        try {
            //Request
            JSONObject json;
            if(params.type == LocationSharingParams.TYPE_GET || params.type == LocationSharingParams.TYPE_CREATE || params.type == LocationSharingParams.TYPE_CANCEL){
                 //Params
                 List<NameValuePair> requestParams = (List<NameValuePair>) getRequestParams();
                 json = jsonParser.makeHttpRequest(LOCATION_SHARING_URL, params.type, requestParams);
            }else{
                JSONObject jsonObject = (JSONObject) getRequestParams();
                json = jsonParser.makeHttpRequest(LOCATION_SHARING_URL, params.type, null, jsonObject);
            }

            //Results
            success = json.getInt(AppConstants.TAG_SUCCESS);
            result.successCode = success;
            message = json.getString(AppConstants.TAG_MESSAGE);
            result.message = message;

            if(result.successCode == 1){
                switch(params.type){
                    case LocationSharingParams.TYPE_GET:
                        return getHaystackList(json, result);
                    case LocationSharingParams.TYPE_CREATE:
                        return getCreatedHaystack(json, result);
                    case LocationSharingParams.TYPE_UPDATE:
                        //return getUpdatedLocationSharing(json, result);
                        return null;
                    case LocationSharingParams.TYPE_CANCEL:
                        // return getCancelledLocationSharing(json, result);
                        return null;

                }
            }else{
                Log.i(TAG, "error");
                result.message = "CreateHaystack Failure! " + result.message;
                Log.d(TAG, result.message);
            }


            return result;
        } catch (Exception e) {
            result.message = "CreateHaystack Failure! " + e.getMessage();
            Log.d(TAG, result.message);

            e.printStackTrace();
        }

        return result;
    }

    private Object getRequestParams() {
        List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
        JSONObject jsonObject = new JSONObject();
        switch (params.type){
            case HaystackTaskParams.TYPE_GET :
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_USER_ID, String.valueOf(params.userId)));
                return requestParams;
            case LocationSharingParams.TYPE_CREATE :
                requestParams.add(new BasicNameValuePair("name", params.vo.getName()));
                requestParams.add(new BasicNameValuePair("owner", String.valueOf(params.vo.getOwner())));
                requestParams.add(new BasicNameValuePair("isPublic", (params.vo.getIsPublic()) ? "1" : "0"));
                requestParams.add(new BasicNameValuePair("timeLimit", params.vo.getTimeLimit()));
                requestParams.add(new BasicNameValuePair("zoneRadius", String.valueOf(params.vo.getZoneRadius())));
                requestParams.add(new BasicNameValuePair("isCircle", (params.vo.getIsPublic()) ? "1" : "0"));
                requestParams.add(new BasicNameValuePair("lat", String.valueOf(params.vo.getPosition().latitude)));
                requestParams.add(new BasicNameValuePair("lng", String.valueOf(params.vo.getPosition().longitude)));
                requestParams.add(new BasicNameValuePair("pictureURL", params.vo.getPictureURL()));

                int i;
                ArrayList<UserVO> haystackUsers = params.vo.getUsers();
                for(i=0;i<haystackUsers.size();i++){
                    UserVO user = haystackUsers.get(i);
                    requestParams.add(new BasicNameValuePair("haystack_user[]", String.valueOf(user.getUserId())));
                }

                ArrayList<UserVO> haystackActiveUsers = params.vo.getActiveUsers();
                for(i=0;i<haystackActiveUsers.size();i++){
                    UserVO user = haystackActiveUsers.get(i);
                    requestParams.add(new BasicNameValuePair("haystack_active_user[]", String.valueOf(user.getUserId())));
                }

                ArrayList<UserVO> haystackBannedUsers = params.vo.getBannedUsers();
                for(i=0;i<haystackBannedUsers.size();i++){
                    UserVO user = haystackBannedUsers.get(i);
                    requestParams.add(new BasicNameValuePair("haystack_banned_user[]", String.valueOf(user.getUserId())));
                }

                return requestParams;
            case LocationSharingParams.TYPE_UPDATE :
                /*try{
                    jsonObject.put(AppConstants.TAG_RECEIVER_ID, String.valueOf(params.vo.getReceiverId()));
                    jsonObject.put(AppConstants.TAG_LOCATION_SHARING_ID, String.valueOf(params.vo.getId()));
                    jsonObject.put(AppConstants.TAG_SHARE_BACK, String.valueOf(!params.vo.getShareBack()));
                }catch (Exception e){
                    Log.i(TAG, "Exception : "+e.getMessage());
                }

                return jsonObject;*/
            case LocationSharingParams.TYPE_CANCEL :
               /* requestParams.add(new BasicNameValuePair(AppConstants.TAG_SENDER_ID, String.valueOf(params.vo.getSenderId())));
                requestParams.add(new BasicNameValuePair(AppConstants.TAG_LOCATION_SHARING_ID, String.valueOf(params.vo.getId())));

                return requestParams;*/
        }

        return null;
    }

    private HaystackTaskResult getHaystackList(JSONObject json, HaystackTaskResult result){
        ArrayList<Object> haystackList = null;
        ArrayList<HaystackVO> publicHaystackList = null;
        ArrayList<HaystackVO> privateHaystackList = null;

        try {
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
                    JSONObject locationData = haystackData.getJSONObject(AppConstants.TAG_LOCATION);
                    haystack.setPosition(new LatLng(locationData.getDouble(AppConstants.TAG_LATITUDE), locationData.getDouble(AppConstants.TAG_LONGITUDE)));
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

    private HaystackTaskResult getCreatedHaystack(JSONObject json, HaystackTaskResult result){
        try{
            JSONObject haystack = (JSONObject) json.get(AppConstants.TAG_HAYSTACK);
            params.vo.setId(haystack.getInt(AppConstants.TAG_ID));

            result.haystack = params.vo;
            result.message = json.getString(AppConstants.TAG_MESSAGE);

            Log.d(TAG, "Haystack Created Successfuly! " + json.getString(AppConstants.TAG_MESSAGE));
        }catch (JSONException e) {
            e.printStackTrace();
            result.message = "Location Sharing could not be created. Exception : " + e.getMessage();
            result.successCode = 0;
        }

        Log.d(TAG, "Location Sharing Created Successfuly! " + result.message);

        return result;
    }

    //Interfaces
    public interface CreateHaystackResponseHandler {
        void onHaystackCreated(HaystackTaskResult result);
    }

    public interface FetchHaystackResponseHandler {
        void onHaystackFetched(HaystackTaskResult result);
    }
}
