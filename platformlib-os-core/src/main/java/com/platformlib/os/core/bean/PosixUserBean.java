package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.posix.PosixUser;

public class PosixUserBean extends IdNamePairBean implements PosixUser {
    public PosixUserBean(final int id, final String name) {
        super(id, name);
    }
}
