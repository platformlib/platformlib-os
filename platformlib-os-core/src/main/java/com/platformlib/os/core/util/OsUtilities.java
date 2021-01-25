package com.platformlib.os.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * OS utility class.
 */
public final class OsUtilities {
    private static final String OS_COMMAND_PROPERTY_PREFIX = "com.platformlib.os.command.";
    private static final int BUFFER_SIZE = 512;

    /**
     * Closed constructor.
     */
    private OsUtilities() {
    }

    /**
     * Get OS command.
     * The OS command could be changed via system or environment property.
     * @param command command
     * @return Returns command to use
     */
    public static String getOsCommand(final String command) {
        final String osCommandProperty = OS_COMMAND_PROPERTY_PREFIX + command;
        final String osCommandPropertyValue = System.getProperty(osCommandProperty, System.getenv(osCommandProperty));
        return osCommandPropertyValue == null ? command : osCommandPropertyValue;
    }

    /**
     * Filter digits from OS version.
     * @param version os version
     * @return Returns extracted digits and '.' from os version
     */
    public static String filterVersionDigits(final String version) {
        final StringBuilder result = new StringBuilder();
        for (char versionChar: version.toCharArray()) {
            if (Character.isDigit(versionChar) ||  '.' == versionChar) {
                result.append(versionChar);
            }
        }
        return result.toString();
    }

    /**
     * Extract digits from source string.
     * Stop extracting on first non digit char.
     * @param sourceString input string
     * @return Returns digits extracted from input
     */
    public static Optional<Integer> parseVersionDigits(final String sourceString) {
        final StringBuilder result = new StringBuilder();
        for (char versionChar: sourceString.toCharArray()) {
            if (!Character.isDigit(versionChar)) {
                break;
            }
            result.append(versionChar);
        }
        if (result.length() == 0) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(result.toString()));
    }

    /**
     * Read all bytes from input stream.
     * @param is input stream
     * @return Returns read bytes
     * @throws IOException if I/O error occurs
     */
    public static byte[] readInputStream(final InputStream is) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int len;
        while ((len = is.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

}
