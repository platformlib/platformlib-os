package com.platformlib.os.api.osi.posix;

import com.platformlib.os.api.osi.OsInterface;

/**
 * Posix OS interface.
 */
public interface PosixOsInterface extends OsInterface {
    @Override
    PosixOsUser getCurrentUser();
}
