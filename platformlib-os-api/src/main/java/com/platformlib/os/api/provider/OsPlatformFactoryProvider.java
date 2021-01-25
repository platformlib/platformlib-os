package com.platformlib.os.api.provider;

import com.platformlib.os.api.OsPlatform;

/**
 * OS platform factory provider.
 * @param <T> OS platform type
 */
public interface OsPlatformFactoryProvider<T extends OsPlatform> {
    /**
     * Check if OS platform is suitable for given specification.
     * @param specification specification
     * @return Returns true if platform is suitable, false otherwise
     */
    boolean isSuitable(Object specification);

    /**
     * Create new OS platform.
     * @param specification specification
     * @return Returns created OS platform
     */
    T newOsPlatform(Object specification);
}
