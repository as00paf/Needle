package com.nemator.needle.models.vo;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.UserVO;

/**
 * Created by Alex on 21/02/2016.
 */
public class HaystackUserVO {

    @SerializedName("haystack")
    private HaystackVO haystack;

    @SerializedName("user")
    private UserVO user;

    public HaystackUserVO(HaystackVO haystack, UserVO user) {
        super();
        this.haystack = haystack;
        this.user = user;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public HaystackVO getHaystack() {
        return haystack;
    }

    public void setHaystack(HaystackVO haystack) {
        this.haystack = haystack;
    }
}
