package com.tinkerpop.frames.modules;

/**
 * This allows the user to provide a custom ClassLoaderResolver. In some
 * cases, the user may wish for a type to be framed with a non-default
 * classloader (for example, if supplementary types from a TypeResolver
 * need to be loaded from some other classloader).
 * 
 * This resolution system allows the user to provide their own custom classloader
 * resolver.
 *
 * Instances of this class should be threadsafe.
 * 
 * @author Jess Sightler <jesse.sightler@gmail.com>
 */
public interface FrameClassLoaderResolver {
    public ClassLoader resolveClassLoader(Class<?> frameType);
}
