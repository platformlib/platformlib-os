package com.platformlib.os.api.osi;

/**
 * Disk space usage structure.
 */
public interface DiskSpaceInfo {
    /**
     * Get used bytes.
     * @return Returns used bytes.
     */
    long getUsed();

    /**
     * Get available bytes.
     * @return Returns available bytes.
     */
    long getAvailable();
}
