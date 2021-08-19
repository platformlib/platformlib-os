package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.windows.WindowsOsUser;

import java.util.Optional;

public class WindowsOsUserBean extends OsUserBean implements WindowsOsUser {
    private final String domain;

    public WindowsOsUserBean(final String username, final String domain) {
        super(username);
        this.domain = domain;
    }

    @Override
    public Optional<String> getDomain() {
        return Optional.ofNullable(domain);
    }
}
