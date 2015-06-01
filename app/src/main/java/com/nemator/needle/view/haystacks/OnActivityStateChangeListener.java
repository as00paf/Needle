package com.nemator.needle.view.haystacks;

public interface OnActivityStateChangeListener {
    public void onStateChange(int state);
    public int getCurrentState();
    public int getPreviousState();
}
