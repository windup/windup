package org.jboss.windup.rules.files;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface FileDiscoveredListener
{
    FileDiscoveredResult fileDiscovered(FileDiscoveredEvent event);
}
