package org.jboss.windup.windride.spi;

import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ex.MigrationException;

/**
 *  Simple WindRide addon service - takes the WindRide arguments as input.
 */
public interface IWindRideService 
{
    public void doMigration( Configuration conf ) throws MigrationException;
    
}
