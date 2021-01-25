package com.platformlib.os.api.osi.windows;

import com.platformlib.os.api.osi.OsUser;

import java.util.Optional;

/**
 * Windows username.
 */
public interface WindowsOsUser extends OsUser {
    /**
     * Get domain.
     * @return Return domain name if logged in domain, {@link Optional#empty()} otherwise
     */
    Optional<String> getDomain();
}
