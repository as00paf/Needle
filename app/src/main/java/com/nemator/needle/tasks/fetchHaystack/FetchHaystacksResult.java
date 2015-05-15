package com.nemator.needle.tasks.fetchHaystack;

import com.nemator.needle.models.vo.HaystackVO;

import java.util.ArrayList;

public class FetchHaystacksResult {
    public ArrayList<Object> haystackList;
    public int successCode;

    public ArrayList<HaystackVO> publicHaystackList = null;
    public ArrayList<HaystackVO> privateHaystackList = null;

    public FetchHaystacksResult(){

    }

}
