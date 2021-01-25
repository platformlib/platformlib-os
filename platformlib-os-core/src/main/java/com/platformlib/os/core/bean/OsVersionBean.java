package com.platformlib.os.core.bean;

import com.platformlib.os.api.osi.OsVersion;

import java.util.Optional;

/**
 * Bean class for {@link OsVersion}.
 */
public class OsVersionBean implements OsVersion {
    private final int major;
    private final Integer minor;

    public OsVersionBean(int major, Integer minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public int getMajor() {
        return major;
    }

    @Override
    public Optional<Integer> getMinor() {
        return Optional.ofNullable(minor);
    }

    @Override
    public String toString() {
        return "OsVersionImpl{" +
                "major=" + major +
                ", minor=" + minor +
                '}';
    }
}
