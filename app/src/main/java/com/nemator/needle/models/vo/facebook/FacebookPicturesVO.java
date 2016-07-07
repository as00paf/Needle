package com.nemator.needle.models.vo.facebook;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Alex on 13/02/2016.
 */
public class FacebookPicturesVO implements Serializable {

    @SerializedName("data")
    private ArrayList<FacebookPictureDataVO> data;

    protected FacebookPicturesVO(Parcel in) {
        data = in.readParcelable(FacebookPictureDataVO.class.getClassLoader());
    }

    public ArrayList<FacebookPictureDataVO> getData() {
        return data;
    }

    public void setData(ArrayList<FacebookPictureDataVO> data) {
        this.data = data;
    }

    public FacebookPictureDataVO get(int position) {
        return data.get(position);
    }
}
