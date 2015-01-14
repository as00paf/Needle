package com.needletest.pafoid.needletest.home.task;

import com.needletest.pafoid.needletest.models.Haystack;

import java.util.ArrayList;

public class FetchHaystacksResult {
    public ArrayList<Object> haystackList;
    public int successCode;

    public ArrayList<Haystack> publicHaystackList = null;
    public ArrayList<Haystack> privateHaystackList = null;

    public FetchHaystacksResult(){

    }

}
