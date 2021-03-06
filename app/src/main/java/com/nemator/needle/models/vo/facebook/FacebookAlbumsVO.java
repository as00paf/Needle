package com.nemator.needle.models.vo.facebook;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.adapter.FacebookAlbumAdapter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Alex on 13/02/2016.
 */
public class FacebookAlbumsVO implements Serializable {

    @SerializedName("data")
    private ArrayList<FacebookAlbumVO> data;

    protected FacebookAlbumsVO(Parcel in) {
        data = in.readParcelable(FacebookPictureDataVO.class.getClassLoader());
    }

    public ArrayList<FacebookAlbumVO> getData() {
        return data;
    }

    public void setData(ArrayList<FacebookAlbumVO> data) {
        this.data = data;
    }

    public FacebookAlbumVO get(int position) {
        return data.get(position);
    }
}
