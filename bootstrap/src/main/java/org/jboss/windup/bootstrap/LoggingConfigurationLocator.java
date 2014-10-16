/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.windup.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.logmanager.ConfigurationLocator;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class LoggingConfigurationLocator implements ConfigurationLocator
{
    static final FilenameFilter LOGGING_CONFIG_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(final File dir, final String name)
        {
            return name.equals("logging.properties");
        }
    };

    @Override
    public InputStream findConfiguration() throws IOException
    {
        // First look for the property
        final String propLoc = System.getProperty("logging.configuration");
        if (propLoc != null)
            try
            {
                return new URL(propLoc).openStream();
            }
            catch (IOException e)
            {
                System.err.printf("Unable to read the logging configuration from '%s' (%s)%n", propLoc, e);
            }
        File[] files = null;
        // Second attempt to find the configuration in the users .forge directory
        final File userWindupDir = Bootstrap.getUserWindupDir();
        // Look for a logging.properties file
        if (userWindupDir.isDirectory())
        {
            files = userWindupDir.listFiles(LOGGING_CONFIG_FILTER);
            if (files != null && files.length > 0)
            {
                return new FileInputStream(files[0]);
            }
        }
        // Finally default to $FORGE_HOME/logging.properties
        final File windupHomeDir = OperatingSystemUtils.getForgeHomeDir();
        // Look for a logging.properties file
        if (windupHomeDir.isDirectory())
        {
            files = windupHomeDir.listFiles(LOGGING_CONFIG_FILTER);
        }
        // If the file was found, return it, otherwise return null
        if (files != null && files.length > 0)
        {
            return new FileInputStream(files[0]);
        }
        System.err.println("No logging configuration was found.");
        return null;
    }
}
