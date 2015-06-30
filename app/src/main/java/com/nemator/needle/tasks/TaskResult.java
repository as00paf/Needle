package com.nemator.needle.tasks;

public class TaskResult {
    public int successCode;
    public String message;

    public TaskResult(int successCode, String message){
        this.successCode = successCode;
        this.message = message;
    }

    public TaskResult(){}
}
