package org.jboss.windup.util.furnace;

import org.jboss.forge.furnace.util.Predicate;

/**
 * Filters filenames by file extension.
 *
 */
public class FileExtensionFilter implements Predicate<String>
{
    private String extension;

    /**
     * Only accept names that end with "." + extension.
     */
    public FileExtensionFilter(String extension)
    {
        super();
        this.extension = extension;
    }

    @Override
    public boolean accept(String name)
    {
        return name.endsWith("." + extension);
    }
}
