/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.parser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.loader.WindupRuleProviderLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.furnace.FileExtensionFilter;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;
import org.w3c.dom.Document;

/**
 * This {@link WindupRuleProviderLoader} searches for and loads {@link WindupRuleProvider}s from XML files that within all addons, with filenames that
 * end in ".windup.xml".
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XMLRuleProviderLoader implements WindupRuleProviderLoader
{
    private static final Logger LOG = Logging.get(XMLRuleProviderLoader.class);

    public static final String CURRENT_WINDUP_SCRIPT = "CURRENT_WINDUP_SCRIPT";

    private static final String XML_RULES_EXTENSION = "windup.xml";

    @Inject
    private Furnace furnace;
    @Inject
    private FurnaceClasspathScanner scanner;

    @Override
    public List<WindupRuleProvider> getProviders(GraphContext context)
    {
        List<WindupRuleProvider> providers = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = null;

        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
        }
        catch (Exception e)
        {
            throw new WindupException("Failed to build xml parser due to: " + e.getMessage(), e);
        }

        for (Map.Entry<Addon, List<URL>> addonFiles : getAddonWindupXmlFiles().entrySet())
        {
            Addon addon = addonFiles.getKey();
            List<URL> urls = addonFiles.getValue();
            for (URL resource : urls)
            {
                try
                {
                    Document doc = dBuilder.parse(resource.toURI().toString());
                    ParserContext parser = new ParserContext(furnace);

                    parser.setAddonContainingInputXML(addon);

                    parser.processElement(doc.getDocumentElement());
                    providers.addAll(parser.getRuleProviders());
                }
                catch (Exception e)
                {
                    throw new WindupException("Failed to parse XML configuration at: " + resource.toString()
                                + " due to: "
                                + e.getMessage(), e);
                }
            }
        }

        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);
        for (FileModel userRulesFileModel : cfg.getUserRulesPaths())
        {
            for (URL resource : getWindupUserDirectoryXmlFiles(userRulesFileModel))
            {
                try
                {
                    Document doc = dBuilder.parse(resource.toURI().toString());
                    ParserContext parser = new ParserContext(furnace);

                    String userRulesPath = userRulesFileModel.getFilePath();
                    parser.setXmlInputPath(Paths.get(userRulesPath));

                    parser.processElement(doc.getDocumentElement());
                    providers.addAll(parser.getRuleProviders());
                }
                catch (Exception e)
                {
                    throw new WindupException("Failed to parse XML configuration at: " + resource.toString() + " due to: "
                                + e.getMessage(), e);
                }
            }
        }

        return providers;
    }

    private Map<Addon, List<URL>> getAddonWindupXmlFiles()
    {
        return scanner.scanForAddonMap(new FileExtensionFilter(XML_RULES_EXTENSION));
    }

    private Collection<URL> getWindupUserDirectoryXmlFiles(FileModel userRulesFileModel)
    {
        String userRulesDirectory = userRulesFileModel == null ? null : userRulesFileModel.getFilePath();

        // no user dir, so just return the ones that we found in the classpath
        if (userRulesDirectory == null)
        {
            return Collections.emptyList();
        }
        Path userRulesPath = Paths.get(userRulesDirectory);
        if (!Files.isDirectory(userRulesPath))
        {
            LOG.warning("Not scanning: " + userRulesPath.normalize().toString() + " for rules as the directory could not be found!");
            return Collections.emptyList();
        }
        if (!Files.isDirectory(userRulesPath))
        {
            LOG.warning("Not scanning: " + userRulesPath.normalize().toString() + " for rules as the directory could not be read!");
            return Collections.emptyList();
        }

        // create the results as a copy (as we will be adding user groovy files to them)
        final List<URL> results = new ArrayList<>();

        try
        {
            Files.walkFileTree(userRulesPath, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    if (file.getFileName().toString().toLowerCase().endsWith("." + XML_RULES_EXTENSION))
                    {
                        results.add(file.toUri().toURL());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to search userdir: \"" + userRulesDirectory + "\" for groovy rules due to: "
                        + e.getMessage(), e);
        }

        return results;
    }
}
