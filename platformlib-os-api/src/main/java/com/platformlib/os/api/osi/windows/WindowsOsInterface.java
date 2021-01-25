package com.platformlib.os.api.osi.windows;

import com.platformlib.os.api.osi.OsInterface;

/**
 * Windows OS interface.
 */
public interface WindowsOsInterface extends OsInterface {
    @Override
    WindowsOsUser getCurrentUser();
}
