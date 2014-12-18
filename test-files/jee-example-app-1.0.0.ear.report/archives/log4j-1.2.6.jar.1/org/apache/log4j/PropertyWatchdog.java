package org.apache.log4j;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.FileWatchdog;

class PropertyWatchdog extends FileWatchdog{
    PropertyWatchdog(final String filename){
        super(filename);
    }
    public void doOnChange(){
        new PropertyConfigurator().doConfigure(super.filename,LogManager.getLoggerRepository());
    }
}
