package org.jboss.windup.rules.apps.java.scan.ast;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.windup.graph.model.ProjectModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaAnalysisBatch {
    private ProjectModel application;
    private Set<Path> sourceFiles = new LinkedHashSet<>();
    private Set<String> classpath = new LinkedHashSet<>();

    public JavaAnalysisBatch() {
    }

    public JavaAnalysisBatch(ProjectModel application) {
        this.application = application;
    }

    public Set<Path> getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(Set<Path> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public Set<String> getClasspath() {
        return classpath;
    }

    public void setClasspath(Set<String> classpath) {
        this.classpath = classpath;
    }
}
