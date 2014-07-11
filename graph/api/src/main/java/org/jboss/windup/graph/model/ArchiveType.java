package org.jboss.windup.graph.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to associate this particular Archive Model with a file extension.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ArchiveType
{
    /**
     * The archive file extension (eg, ".war" or ".jar")
     */
    public String value();
}
