package org.jboss.windup.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A composite class loader which looks for classes and resources in a list of class loaders.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class FurnaceCompositeClassLoader extends ClassLoader {
    private final Set<ClassLoader> loaders = new LinkedHashSet<>();

    public FurnaceCompositeClassLoader(List<ClassLoader> loaders) {
        this.loaders.addAll(loaders);
    }

    public FurnaceCompositeClassLoader(ClassLoader classLoader, List<ClassLoader> loaders) {
        super(classLoader);
        this.loaders.addAll(loaders);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader classLoader : loaders) {
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException notFound) {
                // oh well
            }
        }

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            return contextLoader.loadClass(name);
        } else {
            return super.findClass(name);
        }
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader classLoader : loaders) {
            URL resource = classLoader.getResource(name);
            if (resource != null) {
                return resource;
            }
        }

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            return contextLoader.getResource(name);
        } else {
            return super.getResource(name);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Set<URL> result = new LinkedHashSet<>();
        for (ClassLoader classLoader : loaders) {
            result.addAll(Collections.list(classLoader.getResources(name)));
        }

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            result.addAll(Collections.list(contextLoader.getResources(name)));
        } else {
            result.addAll(Collections.list(super.getResources(name)));
        }
        return Collections.enumeration(result);
    }
}