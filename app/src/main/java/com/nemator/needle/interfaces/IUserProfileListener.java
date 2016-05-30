package com.nemator.needle.interfaces;

import com.nemator.needle.models.vo.UserVO;

public interface IUserProfileListener {
    void unfriend(UserVO user);
    void sendFriendRequest(UserVO user);
    void acceptFriendRequest(UserVO user);
    void addUserToGroup(UserVO user);
    void sendNeedle(UserVO user);
    void cancelFriendRequest(UserVO user);
    void rejectFriendRequest(UserVO user);
}
