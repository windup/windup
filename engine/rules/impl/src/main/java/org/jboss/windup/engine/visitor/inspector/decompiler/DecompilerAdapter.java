package org.jboss.windup.engine.visitor.inspector.decompiler;

import java.io.File;

/**
 * Facade to the Decompiler implementation.
 * 
 * @author bdavis
 * 
 */
public interface DecompilerAdapter {
    public abstract void decompile(String className, String classLocation, String sourceOutputLocation);

    public abstract void decompile(String className, File classLocation, File sourceOutputLocation);
}
