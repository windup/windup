package org.jboss.windup.util.furnace;

public class FurnaceScannerFileExtensionFilenameFilter implements FurnaceScannerFilenameFilter
{
    private String extension;

    public FurnaceScannerFileExtensionFilenameFilter(String extension)
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
