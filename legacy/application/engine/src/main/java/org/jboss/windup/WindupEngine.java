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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.windup.interrogator.DirectoryInterrogationEngine;
import org.jboss.windup.interrogator.FileInterrogationEngine;
import org.jboss.windup.interrogator.ZipInterrogationEngine;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.util.LogController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class WindupEngine {
    private static final Logger LOG = LoggerFactory.getLogger(WindupEngine.class);
    private static final String USER_PROVIDED_FILE_SUFFIX = ".windup.xml";

    private ApplicationContext context;
    private List<String> supportedExtensions;

    private ZipInterrogationEngine interrogationEngine;
    private DirectoryInterrogationEngine directoryInterrogationEngine;
    private FileInterrogationEngine fileInterrogationEngine;

    private WindupEnvironment settings;

    public WindupEngine(WindupEnvironment settings) {
        setupEnvironment(settings);
        this.settings = settings;

        // sets environment variables needed for Spring configuration.
        
        loadRuleContextFiles();

        interrogationEngine = (ZipInterrogationEngine) context.getBean("archive-interrogation-engine");
        directoryInterrogationEngine = (DirectoryInterrogationEngine) context.getBean("directory-interrogation-engine");
        fileInterrogationEngine = (FileInterrogationEngine) context.getBean("file-interrogation-engine");
        supportedExtensions = new ArrayList((Collection<String>) context.getBean("zipExtensions"));
    }

    private void loadRuleContextFiles() {
        List<String> springContexts = new LinkedList<String>();
        LOG.info("Should load rules contexts files... exclude base rules? " + settings.isExcludeBuiltinRules() + " user rules dir: " + settings.getSupplementalRulesDirectory());

        if (!this.settings.isExcludeBuiltinRules()) {
            springContexts.add("/jboss-windup-context.xml");
            springContexts.add("classpath*:/windup/**/*.windup.xml");
            springContexts.add("classpath*:/*.windup.xml");
        }
        this.context = new ClassPathXmlApplicationContext(springContexts.toArray(new String[springContexts.size()]));
        
        File parentDir = this.settings.getSupplementalRulesDirectory();
        if (parentDir == null || !parentDir.isDirectory()) {
            return;
        } else {
            String springContextAdditionalPath = parentDir.toURI() + "/**/*.windup.xml";
            String[] springContextAdditionalPathArr = new String[] { springContextAdditionalPath };
            FileSystemXmlApplicationContext newContext = new FileSystemXmlApplicationContext(springContextAdditionalPathArr, true, this.context);
            this.context = newContext;
        }
    }
    
    public ApplicationContext getContext() {
        return context;
    }

    /**
     * Sets up runtime properties for the context.
     * 
     * @param settings
     */
    private void setupEnvironment(WindupEnvironment settings) {
        // validate settings...
        if (settings.getIncludeJavaPackageSignature() != null) {
            String javaPkgSigs = StringUtils.join(settings.getIncludeJavaPackageSignature(), ":");
            System.setProperty("package.signature", javaPkgSigs);
        }
        else {
            LOG.warn("WARNING: Consider specifying javaPkgs.  Otherwise, the Java code will not be inspected.");
        }

        if (settings.getExcludeJavaPackageSignature() != null) {
            String excSigString = StringUtils.join(settings.getExcludeJavaPackageSignature(), ":");
            System.setProperty("exclude.signature", excSigString);
        }


        if (StringUtils.isNotBlank(settings.getTargetPlatform())) {
            System.setProperty("target.platform", settings.getTargetPlatform());
        }

        if (settings.isFetchRemote()) {
            System.setProperty("fetch.remote", String.valueOf(settings.isFetchRemote()));
        }
        else {
            LOG.warn("INFO: Will not try and fetch remote versions for unknown JARs.  Consider using: '-fetchRemote true' command line for more detailed reporting.  Requires internet connection.");
            System.setProperty("fetch.remote", "false");
        }

    }

    /**
     * <p>
     * Process a single file.
     * </p>
     * 
     * @param filePath
     * @throws IOException
     */
    public FileMetadata processFile(File file) throws IOException {
        Validate.notNull(file, "File is required, but provided as null.");

        if(!settings.isSource()) {
            throw new RuntimeException("Windup Engine must be set to source mode to process single files.");
        }

        if (!file.exists()) {
            throw new FileNotFoundException("file does not exist: " + file);
        }

        if(!file.isFile()) {
            throw new FileNotFoundException("given file path does not reference a file: " + file.getAbsolutePath());
        }

        return fileInterrogationEngine.process(file);
    }

    /**
     * Processes a directory, recursively, in source mode.  That is, it will not process zip archives, but will treat the directory as a project directory, for example.  
     * 
     * @param dirPath - path to the source directory
     * @param outputPath - path to the report output path
     * @return
     * @throws IOException
     */
    public ArchiveMetadata processSourceDirectory(File dirPath, File outputPath) throws IOException {
        Validate.notNull(dirPath, "Directory input path must be provided.");
        Validate.notNull(dirPath.isDirectory(), "Input must be a directory.");
        Validate.notNull(outputPath, "Directory output path must be provided.");

        File logFile = null;

        if (StringUtils.isNotBlank(settings.getLogLevel())) {
            LogController.setLogLevel(settings.getLogLevel());
        }

        try {
            if (settings.isCaptureLog()) {
                logFile = new File(outputPath.getAbsolutePath() + File.separator + "windup.log");
                LogController.addFileAppender(logFile);
            }
            return directoryInterrogationEngine.process(outputPath, dirPath);
        }
        finally {
            if (logFile != null && logFile.exists()) {
                LogController.removeFileAppender(logFile);
            }
        }
    }


    /**
     * Processes a zip archive and returns the meta information; the report will be output to the outputPath.
     * @param archivePath - path to zip archive
     * @param outputPath - report output path
     * @return
     * @throws IOException
     */
    public ArchiveMetadata processArchive(File archivePath, File outputPath) throws IOException {
        Validate.notNull(archivePath, "Directory archivePath path must be provided.");
        Validate.notNull(outputPath, "Directory outputPath must be provided.");

        if (!archivePath.exists()) {
            throw new FileNotFoundException("Archive to process not found: " + archivePath.getPath());
        }

        File logFile = null;
        if (!outputPath.exists()) {
            FileUtils.forceMkdir(outputPath);
        }

        if (StringUtils.isNotBlank(settings.getLogLevel())) {
            LogController.setLogLevel(settings.getLogLevel());
        }

        try {
            if (settings.isCaptureLog()) {
                logFile = new File(outputPath.getAbsolutePath() + File.separator + "windup.log");
                LogController.addFileAppender(logFile);
            } 
            return interrogationEngine.process(outputPath, archivePath);
        }
        finally {
            if (logFile != null && logFile.exists()) {
                LogController.removeFileAppender(logFile);
            }
        }
    }
}