package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.UserVO;

public class ImageResult extends TaskResult{
    @SerializedName("url")
    private String url;

    public ImageResult(int successCode, String message, String url) {
        super(successCode, message);
        this.url = url;
    }

    public ImageResult() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
