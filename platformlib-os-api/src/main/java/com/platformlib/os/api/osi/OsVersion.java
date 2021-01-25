package com.platformlib.os.api.osi;

import java.util.Optional;

/**
 * OS version.
 * For Solaris returns solaris version not SunOs version.
 */
public interface OsVersion {
    /**
     * Get OS major version.
     * @return Returns OS major version
     */
    int getMajor();

    /**
     * Get OS minor version.
     * @return Returns optionally major version
     */
    Optional<Integer> getMinor();
}
