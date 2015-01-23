package com.needletest.pafoid.needletest.haystack.task.retrieveUsers;

public class RetrieveUsersParams {
    public static final int TYPE_ALL_USERS = 0;
    public static final int TYPE_USERS_NOT_IN_HAYSTACK = 1;

    public String userId;
    public int haystackId;
    public RetrieveUsersParamsType type;

    public enum RetrieveUsersParamsType{
        TYPE_ALL_USERS, TYPE_USERS_NOT_IN_HAYSTACK;
    }

}

