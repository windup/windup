package org.jboss.windup.rules.files;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FileDiscoveredListenerUtil
{
    public static boolean shouldSkip(Iterable<FileDiscoveredListener> listeners, FileDiscoveredEvent event)
    {
        boolean skipFile = false;
        for (FileDiscoveredListener listener : listeners)
        {
            FileDiscoveredResult result = listener.fileDiscovered(event);
            if (result == FileDiscoveredResult.KEEP)
            {
                skipFile = false;
                break;
            }
            else if (result == FileDiscoveredResult.DISCARD)
            {
                skipFile = true;
            }
        }

        return skipFile;
    }
}
