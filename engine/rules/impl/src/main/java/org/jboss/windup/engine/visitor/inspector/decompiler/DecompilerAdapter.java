package org.jboss.windup.engine.visitor.inspector.decompiler;

import java.io.File;

/**
 * Facade to the Decompiler implementation.
 * New API is in the Windup Decompilers API module.
 * 
 * @see  {@link org.jboss.windup.engine.decompiler.api.IDecompiler}
 * @author bdavis
 */
public interface DecompilerAdapter {
    public abstract void decompile(String className, String classLocation, String sourceOutputLocation);

    public abstract void decompile(String className, File classLocation, File sourceOutputLocation);
}
