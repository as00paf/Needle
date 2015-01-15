package com.needletest.pafoid.needletest.haystack.task;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class RetrieveLocationsResult {
    public int successCode;
    public String message;
    public ArrayList<HashMap<String, Object>> locationList;
    public JSONArray locations = null;

}
