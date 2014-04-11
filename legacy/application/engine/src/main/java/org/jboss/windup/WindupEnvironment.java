/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
 */
package org.jboss.windup;

import java.io.File;
import java.util.List;

/***
 * Runtime configuration for the Windup environment
 * 
 * @author bradsdavis
 * 
 */
public class WindupEnvironment {

    private File inputPath;
    private File outputPath;
    private List<String> excludeJavaPackageSignature;
    private List<String> includeJavaPackageSignature;
    private File supplementalRulesDirectory; 
    private String targetPlatform;
    private boolean fetchRemote;
    private boolean excludeBuiltinRules;

    private String logLevel;
    private boolean captureLog;
    private boolean source;

    public void setSource(boolean source) {
        this.source = source;
    }

    public boolean isSource() {
        return source;
    }

    public List<String> getExcludeJavaPackageSignature() {
        return excludeJavaPackageSignature;
    }

    public void setExcludeJavaPackageSignature(List<String> excludeJavaPackageSignature) {
        this.excludeJavaPackageSignature = excludeJavaPackageSignature;
    }

    public List<String> getIncludeJavaPackageSignature() {
        return includeJavaPackageSignature;
    }

    public void setIncludeJavaPackageSignature(
            List<String> packageJavaPackageSignature) {
        this.includeJavaPackageSignature = packageJavaPackageSignature;
    }

    public File getInputPath() {
        return inputPath;
    }

    public void setInputPath(File inputPath) {
        this.inputPath = inputPath;
    }

    public File getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(File outputPath) {
        this.outputPath = outputPath;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public boolean isFetchRemote() {
        return fetchRemote;
    }

    public void setFetchRemote(boolean fetchRemote) {
        this.fetchRemote = fetchRemote;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isCaptureLog() {
        return captureLog;
    }

    public void setCaptureLog(boolean captureLog) {
        this.captureLog = captureLog;
    }

    public File getSupplementalRulesDirectory()
    {
        return supplementalRulesDirectory;
    }
    
    public void setSupplementalRulesDirectory(File supplementalRulesDirectory)
    {
        this.supplementalRulesDirectory = supplementalRulesDirectory;
    }
    
    public boolean isExcludeBuiltinRules()
    {
        return excludeBuiltinRules;
    }
    
    public void setExcludeBuiltinRules(boolean excludeBuiltinRules)
    {
        this.excludeBuiltinRules = excludeBuiltinRules;
    }
}
