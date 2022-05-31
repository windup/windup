/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.parser;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.builder.RuleProviderBuilder;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.loader.RuleProviderLoader;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.furnace.FileExtensionFilter;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

/**
 * This {@link RuleProviderLoader} searches for and loads {@link AbstractRuleProvider}s from XML files that within all
 * addons, with filenames that end in ".windup.xml" or ".rhamt.xml".
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class XMLRuleProviderLoader implements RuleProviderLoader {
    private static final Logger LOG = Logging.get(XMLRuleProviderLoader.class);

    private static final String XML_RULES_WINDUP_EXTENSION = "windup.xml";
    private static final String XML_RULES_RHAMT_EXTENSION = "rhamt.xml";
    private static final String XML_RULES_MTA_EXTENSION = "mta.xml";

    @Inject
    private Furnace furnace;
    @Inject
    private FurnaceClasspathScanner scanner;

    @Override
    public boolean isFileBased() {
        return true;
    }

    @Override
    public List<RuleProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
        List<RuleProvider> providers = new ArrayList<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new WindupException("Failed to build xml parser due to: " + e.getMessage(), e);
        }

        for (Map.Entry<Addon, List<URL>> addonFiles : getAddonWindupXmlFiles().entrySet()) {
            Addon addon = addonFiles.getKey();
            List<URL> urls = addonFiles.getValue();
            for (URL resource : urls) {
                try {
                    Document doc = dBuilder.parse(resource.toURI().toString());
                    ParserContext parser = new ParserContext(furnace, ruleLoaderContext);

                    parser.setAddonContainingInputXML(addon);

                    parser.processElement(doc.getDocumentElement());
                    List<AbstractRuleProvider> parsedProviders = parser.getRuleProviders();
                    setOrigin(parsedProviders, resource);
                    providers.addAll(parsedProviders);
                } catch (Exception e) {
                    throw new WindupException("Failed to parse XML configuration at: " + resource.toString()
                            + " due to: " + e.getMessage(), e);
                }
            }
        }

        for (Path userRulesPath : ruleLoaderContext.getRulePaths()) {
            // Log the files found
            final Collection<URL> userXmlRulesetFiles = getWindupUserDirectoryXmlFiles(userRulesPath);
            StringBuilder sb = new StringBuilder(System.lineSeparator() + "Found " + userXmlRulesetFiles.size() + " user XML rules in: " + userRulesPath);
            for (URL resource : userXmlRulesetFiles)
                sb.append(System.lineSeparator()).append("\t").append(resource.toString());
            LOG.info(sb.toString());

            // Parse each file
            for (URL resource : userXmlRulesetFiles) {
                try {
                    Document doc = dBuilder.parse(resource.toURI().toString());
                    ParserContext parser = new ParserContext(furnace, ruleLoaderContext);

                    parser.setXmlInputPath(Paths.get(resource.toURI()));
                    parser.setXmlInputRootPath(userRulesPath);

                    parser.processElement(doc.getDocumentElement());
                    List<AbstractRuleProvider> parsedProviders = parser.getRuleProviders();
                    setOrigin(parsedProviders, resource);
                    providers.addAll(parsedProviders);
                } catch (Exception e) {
                    throw new WindupException("Failed to parse XML configuration at: " + resource.toString() + " due to: " + e.getMessage(), e);
                }
            }
        }

        return providers;
    }

    private void setOrigin(List<AbstractRuleProvider> providers, URL resource) {
        for (AbstractRuleProvider provider : providers) {
            if (provider instanceof RuleProviderBuilder) {
                ((RuleProviderBuilder) provider).setOrigin(resource.toExternalForm());
            }
        }
    }

    private Map<Addon, List<URL>> getAddonWindupXmlFiles() {
        Map<Addon, List<URL>> addon = scanner.scanForAddonMap(new FileExtensionFilter(XML_RULES_WINDUP_EXTENSION));
        addon.putAll(scanner.scanForAddonMap(new FileExtensionFilter(XML_RULES_RHAMT_EXTENSION)));
        addon.putAll(scanner.scanForAddonMap(new FileExtensionFilter(XML_RULES_MTA_EXTENSION)));
        return addon;
    }

    private Collection<URL> getWindupUserDirectoryXmlFiles(Path userRulesPath) {
        // no user dir, so just return the ones that we found in the classpath
        if (userRulesPath == null)
            return Collections.emptyList();


        try {
            // Deal with the case of a single file here
            if (Files.isRegularFile(userRulesPath) && pathMatchesNamePattern(userRulesPath))
                return Collections.singletonList(userRulesPath.toUri().toURL());

            if (!Files.isDirectory(userRulesPath)) {
                LOG.warning("Not scanning: " + userRulesPath.normalize().toString() + " for rules as the directory could not be read!");
                return Collections.emptyList();
            }

            // create the results as a copy (as we will be adding user xml files to them)
            final List<URL> results = new ArrayList<>();

            Files.walkFileTree(userRulesPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (pathMatchesNamePattern(file)) {
                        results.add(file.toUri().toURL());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return results;
        } catch (IOException e) {
            throw new WindupException("Failed to search userdir: \"" + userRulesPath + "\" for XML rules due to: "
                    + e.getMessage(), e);
        }
    }

    private boolean pathMatchesNamePattern(Path file) {
        return file.getFileName().toString().toLowerCase().endsWith("." + XML_RULES_WINDUP_EXTENSION)
                || file.getFileName().toString().toLowerCase().endsWith("." + XML_RULES_RHAMT_EXTENSION)
                || file.getFileName().toString().toLowerCase().endsWith("." + XML_RULES_MTA_EXTENSION);
    }
}
