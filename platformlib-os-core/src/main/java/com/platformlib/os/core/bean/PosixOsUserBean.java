package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.posix.PosixGroup;
import com.platformlib.os.api.osi.posix.PosixOsUser;
import com.platformlib.os.api.osi.posix.PosixUser;

/**
 * Posix OS user bean.
 */
public class PosixOsUserBean extends OsUserBean implements PosixOsUser {
    private final PosixUser user;
    private final PosixGroup group;

    /**
     * Default constructor.
     * @param user user
     * @param group group
     */
    public PosixOsUserBean(final PosixUser user, final PosixGroup group) {
        super(user.getName());
        this.user = user;
        this.group = group;
    }

    @Override
    public PosixUser getUser() {
        return user;
    }

    @Override
    public PosixGroup getPrimaryGroup() {
        return group;
    }
}
