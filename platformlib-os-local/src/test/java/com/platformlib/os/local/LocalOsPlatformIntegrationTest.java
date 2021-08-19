package com.platformlib.os.local;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.factory.OsPlatforms;
import com.platformlib.os.test.AbstractOsPlatformIntegrationTest;

public class LocalOsPlatformIntegrationTest extends AbstractOsPlatformIntegrationTest {
    @Override
    protected OsPlatform getOsPlatform() {
        return OsPlatforms.getDefaultOsPlatform();
    }

}
