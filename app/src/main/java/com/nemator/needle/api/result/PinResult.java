package com.nemator.needle.api.result;

import com.google.gson.annotations.SerializedName;
import com.nemator.needle.models.vo.PinVO;
import com.nemator.needle.models.vo.UserVO;

public class PinResult extends TaskResult{
    @SerializedName("pin")
    private PinVO pin;

    public PinResult(int successCode, String message, PinVO pin) {
        super(successCode, message);
        this.pin = pin;
    }

    public PinResult() {
    }

    public PinVO getPin() {
        return pin;
    }

    public void setPin(PinVO pin) {
        this.pin = pin;
    }
}
