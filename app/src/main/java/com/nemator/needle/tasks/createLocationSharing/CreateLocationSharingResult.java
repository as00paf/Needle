package com.nemator.needle.tasks.createLocationSharing;

import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.models.vo.LocationSharingVO;
import com.nemator.needle.tasks.createHaystack.CreateHaystackTaskParams;

public class CreateLocationSharingResult {
    public int successCode;
    public String message;
    public LocationSharingVO locationSharing;
    public CreateLocationSharingTaskParams params;
}
