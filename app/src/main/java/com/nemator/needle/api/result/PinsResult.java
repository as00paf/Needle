package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.models.vo.UserVO;

import java.util.ArrayList;

public class PinsResult extends TaskResult{
    @SerializedName("pins")
    private ArrayList<PinVO> pins;

    public PinsResult(int successCode, String message, ArrayList<PinVO> pins) {
        super(successCode, message);
        this.pins = pins;
    }

    public PinsResult() {
    }

    public ArrayList<PinVO> getPins() {
        return pins;
    }

    public void setPins(ArrayList<PinVO> pins) {
        this.pins = pins;
    }
}
