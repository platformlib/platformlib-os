package com.platformlib.os.core.osi;

import com.platformlib.os.api.osi.OsVersion;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.core.bean.OsVersionBean;
import com.platformlib.os.core.util.OsUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacOsInterfaceImpl extends PosixOsInterfaceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacOsInterfaceImpl.class);

    public MacOsInterfaceImpl(final AbstractOsPlatform osPlatform) {
        super(osPlatform);
    }

    @Override
    public final OsVersion getOsVersion() {
        final String swVersCommandOutput = getOsPlatform().shortOsCommand("sw_vers", "-productVersion");
        final String[] macVersionParts = OsUtilities.filterVersionDigits(swVersCommandOutput).split("\\.");
        return new OsVersionBean(OsUtilities.parseVersionDigits(macVersionParts[0]).orElse(-1), macVersionParts.length > 1 ? OsUtilities.parseVersionDigits(macVersionParts[1]).orElse(null) : null);
    }

    @Override
    public int kill(final int pid) {
        throw new IllegalStateException("not implemented");
    }
}
