package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.HaystackVO;

import java.util.ArrayList;

public class HaystackResult extends TaskResult {

    @SerializedName("haystack")
    private HaystackVO haystack;

    @SerializedName("haystacks")
    private ArrayList<Object> haystacks;

    @SerializedName("public_haystacks")
    private ArrayList<HaystackVO> publicHaystacks = null;

    @SerializedName("private_haystacks")
    private ArrayList<HaystackVO> privateHaystacks = null;

    public HaystackResult() {
    }

    public HaystackResult(int successCode, String message, HaystackVO haystack, ArrayList<Object> haystacks, ArrayList<HaystackVO> publicHaystacks, ArrayList<HaystackVO> privateHaystacks) {
        super(successCode, message);
        this.haystack = haystack;
        this.haystacks = haystacks;
        this.publicHaystacks = publicHaystacks;
        this.privateHaystacks = privateHaystacks;
    }

    public HaystackVO getHaystack() {
        return haystack;
    }

    public void setHaystack(HaystackVO haystack) {
        this.haystack = haystack;
    }

    public ArrayList<Object> getHaystacks() {
        return haystacks;
    }

    public void setHaystacks(ArrayList<Object> haystacks) {
        this.haystacks = haystacks;
    }

    public ArrayList<HaystackVO> getPublicHaystacks() {
        return publicHaystacks;
    }

    public void setPublicHaystacks(ArrayList<HaystackVO> publicHaystacks) {
        this.publicHaystacks = publicHaystacks;
    }

    public ArrayList<HaystackVO> getPrivateHaystacks() {
        return privateHaystacks;
    }

    public void setPrivateHaystacks(ArrayList<HaystackVO> privateHaystacks) {
        this.privateHaystacks = privateHaystacks;
    }
}
