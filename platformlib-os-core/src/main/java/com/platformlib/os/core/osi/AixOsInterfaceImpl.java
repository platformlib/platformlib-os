package com.platformlib.os.core.osi;

import com.platformlib.os.api.osi.OsVersion;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.core.bean.OsVersionBean;
import com.platformlib.os.core.util.OsUtilities;

public class AixOsInterfaceImpl extends PosixOsInterfaceImpl {

    public AixOsInterfaceImpl(final AbstractOsPlatform osPlatform) {
        super(osPlatform);
    }

    @Override
    public final OsVersion getOsVersion() {
        final String osLevelContent = getOsPlatform().shortOsCommand("oslevel");
        final String[] aixVersionParts = OsUtilities.filterVersionDigits(osLevelContent).split("\\.");
        return new OsVersionBean(OsUtilities.parseVersionDigits(aixVersionParts[0]).orElse(-1), aixVersionParts.length > 1 ? OsUtilities.parseVersionDigits(aixVersionParts[1]).orElse(null) : null);
    }

    @Override
    public int kill(final int pid) {
        return getOsPlatform()
                .newProcessBuilder()
                .rawExecution()
                .build()
                .execute("/bin/sh", "-c", "ps -ao pid -T " + pid + " | grep -v PID | xargs " + OsUtilities.getOsCommand("kill") + " -9")
                .toCompletableFuture()
                .join().getExitCode();
    }
}
