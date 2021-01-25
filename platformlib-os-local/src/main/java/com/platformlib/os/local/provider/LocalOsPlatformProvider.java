package com.platformlib.os.local.provider;

import com.platformlib.os.api.provider.OsPlatformFactoryProvider;
import com.platformlib.os.local.LocalOsPlatform;

/**
 * Local OS platform provider.
 */
public class LocalOsPlatformProvider implements OsPlatformFactoryProvider<LocalOsPlatform> {
    @Override
    public boolean isSuitable(final Object specification) {
        return specification == null;
    }

    @Override
    public LocalOsPlatform newOsPlatform(final Object specification) {
        return LocalOsPlatform.getInstance();
    }
}
