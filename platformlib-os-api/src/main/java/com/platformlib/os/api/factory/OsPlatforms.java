package com.platformlib.os.api.factory;

import com.platformlib.os.api.OsPlatform;
import com.platformlib.os.api.provider.OsPlatformFactoryProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * OS platforms factory class.
 */
public final class OsPlatforms {

    /**
     * Closed constructor.
     */
    private OsPlatforms() {
    }

    /**
     * Return default OS platform.
     * @param <T> OS platform type
     * @return Returns default OS platform
     */
    public static <T extends OsPlatform> T getDefaultOsPlatform() {
        return newOsPlatform(null);
    }

    /**
     * Create new OS platform
     * @param specification specification
     * @param <T> OS platform type
     * @return Returns create new OS platform
     */
    @SuppressWarnings({"unchecked", "PMD.LawOfDemeter"})
    public static <T extends OsPlatform> T newOsPlatform(final Object specification) {
        final ServiceLoader<OsPlatformFactoryProvider> providers = ServiceLoader.load(OsPlatformFactoryProvider.class);
        final List<OsPlatform> osPlatforms = new ArrayList<>();
        providers.forEach(provider -> {
            if (provider.isSuitable(specification)) {
                osPlatforms.add(provider.newOsPlatform(specification));
            }
        });
        if (osPlatforms.isEmpty()) {
            throw new IllegalStateException("No OS platform has been found for " + specification);
        }
        if (osPlatforms.size() > 1) {
            throw new IllegalStateException("Too OS platform providers have been found for " + specification + ": " + osPlatforms);
        }
        return (T) osPlatforms.get(0);
    }
}
