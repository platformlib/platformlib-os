package com.platformlib.os.test;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.enums.OperationSystem;
import com.platformlib.os.api.enums.OsFamily;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractOsPlatformIntegrationTest {
    abstract protected OsPlatform getOsPlatform();

    protected <R> R osPlatformCall(final Function<OsPlatform, R> osPlatformConsumer) {
        try (OsPlatform osPlatform = getOsPlatform()) {
            return osPlatformConsumer.apply(osPlatform);
        }
    }

    protected void osPlatformTest(final Consumer<OsPlatform> osPlatformConsumer) {
        try (OsPlatform osPlatform = getOsPlatform()) {
            osPlatformConsumer.accept(osPlatform);
        }
    }

    /**
     * Test {@link OsPlatform#getOsSystem()}.
     */
    @Test
    public void testGetOsSystem() {
        final String osName = System.getProperty("os.name");
        //AIX
        //Linux
        //Mac OS X
        //SunOS
        //Windows Server 2019
        //Windows Server 2016
        //Windows Server 2012 R2
        final OperationSystem operationSystem;
        if ("AIX".equals(osName)) {
            operationSystem = OperationSystem.AIX;
        } else if ("Linux".equals(osName)) {
            operationSystem = OperationSystem.LINUX;
        } else if ("Mac OS X".equals(osName)) {
            operationSystem = OperationSystem.MAC;
        } else if ("SunOS".equals(osName)) {
            operationSystem = OperationSystem.SOLARIS;
        } else if (osName.startsWith("Windows")) {
            operationSystem = OperationSystem.WINDOWS;
        } else {
            throw new IllegalStateException(("Unknown OS " + osName));
        }
        osPlatformTest(osPlatform -> assertThat(osPlatform.getOsSystem()).isEqualTo(operationSystem));
    }

    /**
     * Test {@link OsPlatform#getOsFamily()}.
     */
    @Test
    public void testGetOsFamily() {
        osPlatformTest(osPlatform -> assertThat(osPlatform.getOsFamily()).isEqualTo(File.pathSeparatorChar == ';' ? OsFamily.WINDOWS : OsFamily.UNIX));
    }

    /**
     * Test {@link OsPlatform#getEnvVariable(String)}.
     */
    @Test
    public void testGetEnvVariable() {
        osPlatformTest(osPlatform -> assertThat(osPlatform.getEnvVariable("PATH")).hasValue(System.getenv("PATH")));
    }
}
