package com.nemator.needle.utils;

/**
 * Created by Phonetik on 08/12/2014.
 */
public class AppConstants {

    public static final String PROJECT_URL = "http://173.176.76.40:2772/Needle/";
    public static final String HAYSTACK_PICTURES_URL = PROJECT_URL + "pictures/";

    //Json tags
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_MESSAGE = "message";
    public static final String TAG_HAYSTACK_ID = "haystackId";
    public static final String TAG_USER_NAME = "userName";
    public static final String TAG_USER_ID = "userId";
    public static final String TAG_ID = "id";
    public static final String TAG_PICTURE_URL = "pictureURL";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_LOCATIONS = "locations";
    public static final String TAG_LAT = "lat";
    public static final String TAG_LNG = "lng";
    public static final String TAG_USERS = "users";
    public static final String TAG_ADDED_USERS = "addedUserList";
    public static final String TAG_REQUEST_CODE = "requestCode";

    //Location updates
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static String HAYSTACK_DATA_KEY = "data";
    public final static String TAG_IS_OWNER = "isOwner";
    public final static String LOCATION_UPDATED = "locationUpdated";

    //SharedPref keys
    public static final String REQUESTING_LOCATION_UPDATES_KEY = "isRequestingLocationUpdates";
    public static final String LOCATION_KEY = "currentLocation";
    public static final String CUSTOM_LOCATION_KEY = "customLocation";
    public static final String LAST_UPDATED_TIME_STRING_KEY = "lastUpdatedTime";
}
