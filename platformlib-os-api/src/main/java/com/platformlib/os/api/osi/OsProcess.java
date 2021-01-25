package com.platformlib.os.api.osi;

import java.util.Optional;

/**
 * Operation system process.
 */
public interface OsProcess {
    /**
     * Username process owner.
     * @return Returns user who started process
     */
    String getUsername();

    /**
     * Get parent process id.
     * @return Return parent process id if available
     */
    Optional<Integer> getParentProcessId();

    /**
     * Get process identifier.
     * @return Returns process id
     */
    int getProcessId();
}
