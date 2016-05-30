package com.nemator.needle.models.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FriendVO implements Parcelable, Serializable {
    @SerializedName("friendship")
    private FriendshipVO friendship;

    @SerializedName("user")
    private UserVO user;

    public FriendVO(FriendshipVO friendship, UserVO user) {
        this.friendship = friendship;
        this.user = user;
    }

    public FriendVO(Parcel in) {
        this.friendship = in.readParcelable(FriendshipVO.class.getClassLoader());
        this.user = in.readParcelable(UserVO.class.getClassLoader());
    }

    public static final Creator<FriendVO> CREATOR = new Creator<FriendVO>() {
        @Override
        public FriendVO createFromParcel(Parcel in) {
            return new FriendVO(in);
        }

        @Override
        public FriendVO[] newArray(int size) {
            return new FriendVO[size];
        }
    };

    //Getters/Setters
    public FriendshipVO getFriendship() {
        return friendship;
    }

    public void setFriendship(FriendshipVO friendship) {
        this.friendship = friendship;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(friendship, flags);
        dest.writeParcelable(user, flags);
    }
}
