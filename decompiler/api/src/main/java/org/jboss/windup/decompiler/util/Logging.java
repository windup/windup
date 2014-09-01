package org.jboss.windup.decompiler.util;

import java.util.logging.Logger;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Logging {

    /**
     * Shortcut for JUL getLogger().
     */
    public static Logger of(Class cls){
        return Logger.getLogger(cls.getName());
    }
    

}// class
