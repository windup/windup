package org.jboss.windup.util.furnace;

public class FileExtensionFilter implements Filter<String>
{
    private String extension;

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
