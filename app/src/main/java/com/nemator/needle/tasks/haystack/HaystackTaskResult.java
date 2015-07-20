package com.nemator.needle.tasks.haystack;

import com.nemator.needle.models.vo.HaystackVO;

import java.util.ArrayList;

public class HaystackTaskResult {
    public int successCode;
    public String message;
    public HaystackVO haystack;

    public ArrayList<Object> haystackList;
    public ArrayList<HaystackVO> publicHaystackList = null;
    public ArrayList<HaystackVO> privateHaystackList = null;
}
