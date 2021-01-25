package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.OsUser;

/**
 * OS user.
 */
public class OsUserBean implements OsUser {
    private final String username;

    public OsUserBean(final String username) {
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
