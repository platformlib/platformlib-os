package com.platformlib.os.core.osi;

import com.platformlib.os.api.exception.UnsupportedOperationSystemException;
import com.platformlib.os.api.osi.OsVersion;
import com.platformlib.os.core.AbstractOsPlatform;
import com.platformlib.os.core.bean.OsVersionBean;
import com.platformlib.os.core.util.OsUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class SolarisOsInterfaceImpl extends PosixOsInterfaceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolarisOsInterfaceImpl.class);

    public SolarisOsInterfaceImpl(final AbstractOsPlatform osPlatform) {
        super(osPlatform);
    }

    @Override
    public final OsVersion getOsVersion() {
        try (InputStream is = Files.newInputStream(getOsPlatform().getFileSystem().getPath("/etc/release"), StandardOpenOption.READ)) {
            final String releaseFileContent = new String(OsUtilities.readInputStream(is), Charset.defaultCharset());
            LOGGER.trace("Release file content is {}", releaseFileContent);
            final String[] solarisVersionParts = releaseFileContent.trim().split("\\s+");
            return new OsVersionBean(solarisVersionParts.length > 2 ? OsUtilities.parseVersionDigits(solarisVersionParts[2]).orElse(-1) : -1, null);
        } catch (final IOException ioException) {
            throw new UnsupportedOperationSystemException(ioException);
        }
    }

    @Override
    public int kill(int pid) {
        throw new IllegalStateException("Not implemented");
    }
}
