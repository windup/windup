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
package org.jboss.windup.interrogator;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.interrogator.impl.DecoratorPipeline;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.jboss.windup.util.RecursiveZipMetaFactory;


/**
 * Takes the Interrogators, and applies them against the ZipMetadata. It then collects the results, which
 * are returned as an ArchiveResult.
 * 
 * @author bdavis
 */
public class ZipInterrogationEngine {
    private static final Log LOG = LogFactory.getLog(ZipInterrogationEngine.class);

    protected RecursiveZipMetaFactory recursiveExtractor;
    protected DecoratorPipeline<ZipMetadata> decoratorPipeline;

    public void setDecoratorPipeline(DecoratorPipeline<ZipMetadata> decoratorPipeline) {
        this.decoratorPipeline = decoratorPipeline;
    }

    public void setRecursiveExtractor(RecursiveZipMetaFactory recursiveExtractor) {
        this.recursiveExtractor = recursiveExtractor;
    }

    public ZipMetadata process(File outputDirectory, File targetArchive) {

        // Unzip
        ZipMetadata archiveMeta;
        LOG.info("Processing: " + targetArchive.getName());
        try {
            ZipFile zf = new ZipFile(targetArchive);
            archiveMeta = recursiveExtractor.recursivelyExtract(zf);
        }
        catch (Exception e) {
            LOG.error("Error unzipping file: " + targetArchive.getPath(), e);
            return null;
        }

        // Flatten
        List<ZipMetadata> archiveMetas = new LinkedList<ZipMetadata>();
        unfoldRecursion(archiveMeta, archiveMetas);

        int i = 1;
        int j = archiveMetas.size();

        // Process the flattened list.
        for (ZipMetadata archive : archiveMetas) {
            LOG.info("Interrogating (" + i + " of " + j + "): " + archive.getRelativePath());
            File archiveOutputDirectory = new File(outputDirectory + File.separator + archive.getRelativePath()); // Using toString()!
            archive.setArchiveOutputDirectory(archiveOutputDirectory);

            decoratorPipeline.processMeta(archive);
            i++;
        }

        // Delete the extracted files and return the "tree" metadata.
        recursiveExtractor.releaseTempFiles();
        return archiveMeta;
    }

    /**
     *  Fills the collection archiveMetas from the "tree" given in base.
     */
    protected void unfoldRecursion(ZipMetadata base, Collection<ZipMetadata> archiveMetas) {
        for (ArchiveMetadata meta : base.getNestedArchives()) {
            ZipMetadata zipMeta = (ZipMetadata)meta;

            unfoldRecursion(zipMeta, archiveMetas);
        }
        archiveMetas.add(base);
    }

}