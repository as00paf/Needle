package com.nemator.needle.utils;

public class AppConstants {

    public static final String PROJECT_URL = "http://66.131.192.108:2772/Needle/";
    //public static final String PROJECT_URL = "http://192.168.1.107:2772/Needle/"; local
    public static final String HAYSTACK_PICTURES_URL = PROJECT_URL + "pictures/";

    //TODO: put in strings
    public static final String TWITTER_API_KEY = "HfLrGzOyH0XduzvBTX1pvnWPL";
    public static final String TWITTER_API_SECRET = "gXYWKOBs0oBrTSJAE9epiZc8TVRS3GNdl6dq5uTKkp1jHtJDfx";

    //Json tags
    public static final String TAG_TYPE = "type";
    public static final String TAG_USER = "user";
    public static final String TAG_ACTION = "action";
    public static final String TAG_HAYSTACK = "haystack";
    public static final String TAG_LOCATION_SHARING = "locationSharing";
    public static final String TAG_USER_NAME = "userName";
    public static final String TAG_EMAIL = "email";
    public static final String TAG_PASSWORD = "password";
    public static final String TAG_USER_ID = "id";
    public static final String TAG_LOGGED_IN = "loggedIn";
    public static final String TAG_SOCIAL_NETWORK_USER_ID = "socialNetworkUserId";
    public static final String TAG_LOGIN_TYPE = "loginType";
    public static final String TAG_GCM_REG_ID = "gcmRegId";
    public static final String TAG_GCM_REGISTERD = "gcmRegistered";
    public static final String TAG_ID = "id";
    public static final String TAG_PICTURE_URL = "pictureURL";
    public static final String TAG_COVER_PICTURE_URL = "coverPictureURL";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_USERS = "users";
    public static final String TAG_SECTION = "section";
    public static final String TAG_NOTIFICATION = "notification";

    //Location updates
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static String IS_SENT_DATA_KEY = "isSent";
    public final static String TAG_IS_OWNER = "isOwner";
    public final static String TAG_HAYSTACK_NAME = "name";
    public final static String LOCATION_UPDATED = "locationUpdated";

    //SharedPref keys
    public static final String REQUESTING_LOCATION_UPDATES_KEY = "isRequestingLocationUpdates";
    public static final String LOCATION_KEY = "currentLocation";
    public static final String APP_STATE = "appState";
    public static final String APP_PREVIOUS_STATE = "appPrevState";
    public static final String IS_FIRST_START = "isFirstStart";

    //App Sections
    public static final int SECTION_LOGIN = 2;
    public static final int SECTION_HAYSTACKS = 3;
    public static final int SECTION_NEEDLES = 4;
    public static final int SECTION_HELP = 7;
    public static final int SECTION_CREATE_HAYSTACK = 8;
    public static final int SECTION_CREATE_NEEDLE = 9;
    public static final int SECTION_NOTIFICATIONS = 12;
    public static final int SECTION_LOGIN_PICTURE = 13;
    public static final int SECTION_PEOPLE = 14;

    //App
    public static final String GOOGLE_API_CONNECTED = "googleApiConnected";
    public static String SOCIAL_NETWORKS_INITIALIZED = "socialNetworksInitialized";

    //Request Codes
    public static final int SELECT_NEW_HAYSTACK_USERS = 1000;

    //Notification Types
    public static final int HAYSTACK_INVITATION = 0;
    public static final int USER_JOINED_HAYSTACK = 1;
    public static final int USER_LEFT_HAYSTACK = 2;
    public static final int USER_NEEDLE = 3;
    public static final int USER_CANCELLED_NEEDLE = 4;
    public static final int USER_NEEDLE_BACK = 5;
    public static final int USER_STOPPED_NEEDLE = 6;
    public static final int HAYSTACK_CANCELLED = 7;
    public static final int PIN_DELETED_FROM_HAYSTACK = 8;
}
