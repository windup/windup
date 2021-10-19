package org.jboss.windup.rules.apps.openrewrite;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J;

public class WindupJava8Parser implements JavaParser {

    private final JavaParser delegate;

    WindupJava8Parser(JavaParser delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<J.CompilationUnit> parseInputs(Iterable<Parser.Input> sourceFiles, @Nullable Path relativeTo, ExecutionContext ctx) {
        return delegate.parseInputs(sourceFiles, relativeTo, ctx);
    }

    @Override
    public JavaParser reset() {
        return delegate.reset();
    }

    @Override
    public void setClasspath(Collection<Path> classpath) {
        delegate.setClasspath(classpath);
    }

    public static WindupJava8Parser.Builder builder() {
        return new WindupJava8Parser.Builder();
    }

    public static class Builder extends JavaParser.Builder<WindupJava8Parser, WindupJava8Parser.Builder> {

        @Nullable
        private static ClassLoader toolsClassLoader;

        @Nullable
        private static ClassLoader toolsAwareClassLoader;

        static synchronized void lazyInitClassLoaders() {
            if (toolsClassLoader != null && toolsAwareClassLoader != null) {
                return;
            }

            try {
                File tools = Paths.get(System.getProperty("java.home")).resolve("../lib/tools.jar").toFile();
                if (!tools.exists()) {
                    throw new IllegalStateException("To use WindupJava8Parser, you must run the process with a JDK and not a JRE.");
                }

                toolsClassLoader = new URLClassLoader(new URL[]{tools.toURI().toURL()}, WindupJava8Parser.class.getClassLoader());
                ClassLoader appClassLoader = WindupJava8Parser.class.getClassLoader();

                // based on https://developer.jboss.org/thread/202247
                toolsAwareClassLoader = new URLClassLoader(new URL[]{appClassLoader.getResource("/")}, toolsClassLoader) {
                    @Override
                    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                        if (!name.contains("ReloadableJava8") &&
                                !name.startsWith("com.sun.tools") &&
                                !name.startsWith("com.sun.source") &&
                                !name.contains("ReloadableTypeMapping")) {
                            return toolsClassLoader.loadClass(name);
                        }

                        Class<?> loadedClass = findLoadedClass(name);

                        if (loadedClass == null) {
                            try {
                                loadedClass = findClass(name);
                            } catch (ClassNotFoundException e) {
                                loadedClass = super.loadClass(name, resolve);
                            }
                        }

                        if (resolve) {
                            resolveClass(loadedClass);
                        }

                        return loadedClass;
                    }
                };
            } catch (MalformedURLException e) {
                throw new IllegalStateException("To use WindupJava8Parser, you must run the process with a JDK and not a JRE.", e);
            }
        }

        @Override
        public WindupJava8Parser build() {
            lazyInitClassLoaders();

            try {
                // need to reverse this parent/child relationship
                Class<?> reloadableParser = Class.forName("org.openrewrite.java.ReloadableJava8Parser", true,
                        toolsAwareClassLoader);

                Constructor<?> delegateParserConstructor = reloadableParser
                        .getDeclaredConstructor(Collection.class, Collection.class, Collection.class, Charset.class,
                                Boolean.TYPE, Boolean.TYPE, Collection.class);

                delegateParserConstructor.setAccessible(true);

                JavaParser delegate = (JavaParser) delegateParserConstructor
                        .newInstance(classpath, classBytesClasspath, dependsOn, charset, relaxedClassTypeMatching, logCompilationWarningsAndErrors, styles);

                return new WindupJava8Parser(delegate);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to construct WindupJava8Parser.", e);
            }
        }
    }
}
