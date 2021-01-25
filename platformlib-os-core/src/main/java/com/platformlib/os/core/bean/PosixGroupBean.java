package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.posix.PosixGroup;

public class PosixGroupBean extends IdNamePairBean implements PosixGroup {
    public PosixGroupBean(final int id, final String name) {
        super(id, name);
    }
}
