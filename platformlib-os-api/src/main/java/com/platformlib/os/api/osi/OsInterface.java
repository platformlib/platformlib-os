package com.platformlib.os.api.osi;

import java.util.Collection;

/**
 * Platform dependent Operating System Interface.
 */
public interface OsInterface {

    /**
     * Get OS version.
     * @return Returns OS version
     */
    OsVersion getOsVersion();

    /**
     * List OS processes
     * @return Returns collection os OS processes
     */
    Collection<OsProcess> getOsProcesses();

    /**
     * Kill OS process.
     * @param pid to kill
     * @return Returns exit code of platform dependent kill command
     */
    int kill(int pid);

    /**
     * Retrieve disk space usage.
     * @param file file which filesystem disk space info should be retrieved
     * @return Returns disk space usage
     */
    DiskSpaceInfo getDiskSpaceInfo(String file);

    /**
     * Get current user.
     * @return Returns current user
     */
    OsUser getCurrentUser();
}
