package com.platformlib.os.core;

import com.platformlib.os.api.osi.posix.PosixOsInterface;
import com.platformlib.os.core.osi.AixOsInterfaceImpl;
import com.platformlib.os.core.osi.LinuxOsInterfaceImpl;
import com.platformlib.os.core.osi.MacOsInterfaceImpl;
import com.platformlib.os.core.osi.SolarisOsInterfaceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class OsPlatformTest {
    /**
     * Test {@link AbstractOsPlatform#getTypedOsInterface(Class)} for Linux platform.
     */
    @Test
    void testLinuxGetTypedOsInterface() {
        final AbstractOsPlatform abstractOsPlatform = Mockito.mock(AbstractOsPlatform.class, Mockito.CALLS_REAL_METHODS);
        when(abstractOsPlatform.getOsInterface()).thenReturn(new LinuxOsInterfaceImpl(abstractOsPlatform));
        final PosixOsInterface posixOsInterface = abstractOsPlatform.getTypedOsInterface(PosixOsInterface.class);
        assertNotNull(posixOsInterface);
        assertTrue(posixOsInterface instanceof LinuxOsInterfaceImpl);
    }

    /**
     * Test {@link AbstractOsPlatform#getTypedOsInterface(Class)} for AIX platform.
     */
    @Test
    void testAixGetTypedOsInterface() {
        final AbstractOsPlatform abstractOsPlatform = Mockito.mock(AbstractOsPlatform.class, Mockito.CALLS_REAL_METHODS);
        when(abstractOsPlatform.getOsInterface()).thenReturn(new AixOsInterfaceImpl(abstractOsPlatform));
        final PosixOsInterface posixOsInterface = abstractOsPlatform.getTypedOsInterface(PosixOsInterface.class);
        assertNotNull(posixOsInterface);
        assertTrue(posixOsInterface instanceof AixOsInterfaceImpl);
    }

    /**
     * Test {@link AbstractOsPlatform#getTypedOsInterface(Class)} for AIX platform.
     */
    @Test
    void testMacGetTypedOsInterface() {
        final AbstractOsPlatform abstractOsPlatform = Mockito.mock(AbstractOsPlatform.class, Mockito.CALLS_REAL_METHODS);
        when(abstractOsPlatform.getOsInterface()).thenReturn(new MacOsInterfaceImpl(abstractOsPlatform));
        final PosixOsInterface posixOsInterface = abstractOsPlatform.getTypedOsInterface(PosixOsInterface.class);
        assertNotNull(posixOsInterface);
        assertTrue(posixOsInterface instanceof MacOsInterfaceImpl);
    }

    /**
     * Test {@link AbstractOsPlatform#getTypedOsInterface(Class)} for AIX platform.
     */
    @Test
    void testSolarisGetTypedOsInterface() {
        final AbstractOsPlatform abstractOsPlatform = Mockito.mock(AbstractOsPlatform.class, Mockito.CALLS_REAL_METHODS);
        when(abstractOsPlatform.getOsInterface()).thenReturn(new SolarisOsInterfaceImpl(abstractOsPlatform));
        final PosixOsInterface posixOsInterface = abstractOsPlatform.getTypedOsInterface(PosixOsInterface.class);
        assertNotNull(posixOsInterface);
        assertTrue(posixOsInterface instanceof SolarisOsInterfaceImpl);
    }
}
