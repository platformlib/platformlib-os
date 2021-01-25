package com.platformlib.os.api.enums;

/**
 * Operation systems enumeration.
 */
public enum OperationSystem {
    LINUX(OsFamily.UNIX), AIX(OsFamily.UNIX), SOLARIS(OsFamily.UNIX), WINDOWS(OsFamily.WINDOWS), MAC(OsFamily.UNIX);
    private final OsFamily osFamily;

    OperationSystem(final OsFamily osFamily) {
        this.osFamily = osFamily;
    }

    public OsFamily getOsFamily() {
        return osFamily;
    }
}
