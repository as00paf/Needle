package com.nemator.needle.fragments.locationSharing;

import com.nemator.needle.models.vo.LocationSharingVO;

public interface LocationSharingCardListener {
    void onRefreshList();
    void onCancelLocationSharing(LocationSharingVO vo);
}