package com.platformlib.os.local;

import com.platformlib.os.api.factory.OsPlatforms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class LocalOsPlatformFactoryTest {
    @Test
    void testGetLocalOsPlatformInstance() {
        assertSame(OsPlatforms.getDefaultOsPlatform(), OsPlatforms.getDefaultOsPlatform());
        assertSame(LocalOsPlatform.getInstance(), OsPlatforms.getDefaultOsPlatform());
    }
}
