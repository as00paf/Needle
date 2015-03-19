package com.nemator.needle.home.task.fetchHaystack;

import com.nemator.needle.models.Haystack;

import java.util.ArrayList;

public class FetchHaystacksResult {
    public ArrayList<Object> haystackList;
    public int successCode;

    public ArrayList<Haystack> publicHaystackList = null;
    public ArrayList<Haystack> privateHaystackList = null;

    public FetchHaystacksResult(){

    }

}
