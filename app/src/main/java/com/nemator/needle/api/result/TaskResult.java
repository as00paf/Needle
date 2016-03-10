package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;

public class TaskResult {

    @SerializedName("success")
    protected int successCode;

    @SerializedName("message")
    protected String message;

    public TaskResult(int successCode, String message){
        this.successCode = successCode;
        this.message = message;
    }

    public TaskResult(){}

    public int getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(int successCode) {
        this.successCode = successCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
