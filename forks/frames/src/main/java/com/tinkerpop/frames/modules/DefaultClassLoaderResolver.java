package com.tinkerpop.frames.modules;

/**
 * Implements a basic FrameClassLoaderResolver that simply returns
 * the ClassLoader of the provided Frame Type.
 *
 * @author Jess Sightler <jesse.sightler@gmail.com>
 */
public class DefaultClassLoaderResolver implements FrameClassLoaderResolver {
    @Override
    public ClassLoader resolveClassLoader(Class<?> frameType) {
        return frameType.getClassLoader();
    }
}
