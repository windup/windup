package org.jboss.windup.rules.files.condition;

/**
 * This is used to provide a fluent interface for instantiating {@link FileContent}.
 *
 */
public interface FileContentMatches
{
    /**
     * Match filenames against the provided parameterized string.
     */
    FileContent inFilesNamed(String filenamePattern);
}
