package org.jboss.windup.windride.impl;

import org.jboss.loom.MigrationEngine;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.ex.MigrationException;
import org.jboss.windup.windride.spi.WindRideService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class WindRideServiceImpl implements WindRideService
{
    private static final Logger log = LoggerFactory.getLogger(WindRideServiceImpl.class);

    /**
     * 
     * @param conf WindRide configuration.
     * @throws MigrationException for any exception that happens during migration attempt.
     */
    @Override
    public void doMigration(Configuration conf) throws MigrationException
    {

        MigrationEngine migrator = new MigrationEngine(conf);
        migrator.doMigration();

    }

}// class
