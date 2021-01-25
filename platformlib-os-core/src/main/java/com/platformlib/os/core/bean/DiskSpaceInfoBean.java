package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.DiskSpaceInfo;

public class DiskSpaceInfoBean implements DiskSpaceInfo {
    private final long used;
    private final long available;

    public DiskSpaceInfoBean(final long used, final long available) {
        this.used = used;
        this.available = available;
    }

    @Override
    public long getUsed() {
        return used;
    }

    @Override
    public long getAvailable() {
        return available;
    }
}
