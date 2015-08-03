package com.nemator.needle.tasks.haystackUser;

import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.view.locationSharing.LocationSharingListCardAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HaystackUserTaskResult {
    public int successCode;
    public String successMessage;
    public ArrayList<UserVO> users;
    public Boolean isActive;

    public HaystackUserTaskResult(){

    }

}