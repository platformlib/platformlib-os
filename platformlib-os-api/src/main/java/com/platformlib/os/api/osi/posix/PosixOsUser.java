package com.platformlib.os.api.osi.posix;

import com.platformlib.os.api.osi.OsUser;

/**
 * OS user.
 */
public interface PosixOsUser extends OsUser {
    /**
     * Get user.
     * @return Returns posix user
     */
    PosixUser getUser();

    /**
     * Get user's primary group.
     * @return Returns user's primary group
     */
    PosixGroup getPrimaryGroup();
}
