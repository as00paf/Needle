package com.nemator.needle.fragments.needle;

import com.nemator.needle.models.vo.NeedleVO;

public interface NeedleCardListener {
    void onRefreshList();
    void onCancelLocationSharing(NeedleVO vo);
}