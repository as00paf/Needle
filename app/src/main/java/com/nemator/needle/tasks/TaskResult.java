package com.nemator.needle.tasks;

import com.google.gson.annotations.SerializedName;

public class TaskResult {

    @SerializedName("success")
    private int successCode;

    @SerializedName("message")
    private String message;

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
