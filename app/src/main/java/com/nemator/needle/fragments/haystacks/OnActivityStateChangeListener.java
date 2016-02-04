package com.nemator.needle.fragments.haystacks;

public interface OnActivityStateChangeListener {
    public void onStateChange(int state);
    public int getCurrentState();
    public int getPreviousState();
}
