package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.tasks.TaskResult;

import java.util.ArrayList;

public class HaystackTaskResult extends TaskResult {

    @SerializedName("haystack")
    private HaystackVO haystack;

    @SerializedName("haystackList")
    private ArrayList<Object> haystackList;

    @SerializedName("public_haystacks")
    private ArrayList<HaystackVO> publicHaystackList = null;

    @SerializedName("private_haystacks")
    private ArrayList<HaystackVO> privateHaystackList = null;

    public HaystackTaskResult() {
    }

    public HaystackTaskResult(int successCode, String message, HaystackVO haystack, ArrayList<Object> haystackList, ArrayList<HaystackVO> publicHaystackList, ArrayList<HaystackVO> privateHaystackList) {
        super(successCode, message);
        this.haystack = haystack;
        this.haystackList = haystackList;
        this.publicHaystackList = publicHaystackList;
        this.privateHaystackList = privateHaystackList;
    }

    public HaystackVO getHaystack() {
        return haystack;
    }

    public void setHaystack(HaystackVO haystack) {
        this.haystack = haystack;
    }

    public ArrayList<Object> getHaystackList() {
        return haystackList;
    }

    public void setHaystackList(ArrayList<Object> haystackList) {
        this.haystackList = haystackList;
    }

    public ArrayList<HaystackVO> getPublicHaystackList() {
        return publicHaystackList;
    }

    public void setPublicHaystackList(ArrayList<HaystackVO> publicHaystackList) {
        this.publicHaystackList = publicHaystackList;
    }

    public ArrayList<HaystackVO> getPrivateHaystackList() {
        return privateHaystackList;
    }

    public void setPrivateHaystackList(ArrayList<HaystackVO> privateHaystackList) {
        this.privateHaystackList = privateHaystackList;
    }
}
