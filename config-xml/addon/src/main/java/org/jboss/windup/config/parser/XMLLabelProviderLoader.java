package org.jboss.windup.config.parser;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.loader.LabelProviderLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.LabelMetadataBuilder;
import org.jboss.windup.config.metadata.LabelProviderMetadata;
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
 * Searches for and loads {@link LabelProvider}s from XML files. This searches for filenames that ends in ".windup.label.xml" or ".rhamt.label.xml".
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class XMLLabelProviderLoader implements LabelProviderLoader {
    private static final Logger LOG = Logging.get(XMLLabelProviderLoader.class);

    private static final String XML_LABELS_WINDUP_EXTENSION = "windup.label.xml";
    private static final String XML_LABELS_RHAMT_EXTENSION = "rhamt.label.xml";
    private static final String XML_LABELS_MTA_EXTENSION = "mta.label.xml";


    @Inject
    private Furnace furnace;

    @Inject
    private FurnaceClasspathScanner scanner;

    /**
     * {@link LabelProviderLoader#isFileBased()}
     **/
    @Override
    public boolean isFileBased() {
        return true;
    }

    /**
     * {@link LabelProviderLoader#getProviders(RuleLoaderContext)}
     **/
    @Override
    public List<LabelProvider> getProviders(RuleLoaderContext ruleLoaderContext) {
        List<LabelProvider> providers = new ArrayList<>();

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

                    LabelProvider labelProvider = parser.processElement(doc.getDocumentElement());
                    setOrigin(labelProvider, resource);
                    providers.add(labelProvider);
                } catch (Exception e) {
                    throw new WindupException("Failed to parse XML configuration at: " + resource.toString()
                            + " due to: " + e.getMessage(), e);
                }
            }
        }

        for (Path userLabelsPath : ruleLoaderContext.getRulePaths()) {
            // Log the files found
            final Collection<URL> userXmlLabelsetFiles = getWindupUserDirectoryXmlFiles(userLabelsPath);
            StringBuilder sb = new StringBuilder(
                    System.lineSeparator() + "Found " + userXmlLabelsetFiles.size() + " user XML labels in: " + userLabelsPath);
            for (URL resource : userXmlLabelsetFiles) {
                sb.append(System.lineSeparator()).append("\t").append(resource.toString());
            }

            LOG.info(sb.toString());

            // Parse each file
            for (URL resource : userXmlLabelsetFiles) {
                try {
                    Document doc = dBuilder.parse(resource.toURI().toString());
                    ParserContext parser = new ParserContext(furnace, ruleLoaderContext);

                    parser.setXmlInputPath(Paths.get(resource.toURI()));
                    parser.setXmlInputRootPath(userLabelsPath);

                    LabelProvider labelProvider = parser.processElement(doc.getDocumentElement());
                    setOrigin(labelProvider, resource);
                    providers.add(labelProvider);
                } catch (Exception e) {
                    throw new WindupException("Failed to parse XML configuration at: " + resource.toString() + " due to: " + e.getMessage(), e);
                }
            }
        }

        return providers;
    }

    /**
     * Sets {@link LabelProvider} origin using the URL parameter.
     */
    private void setOrigin(LabelProvider provider, URL resource) {
        LabelProviderMetadata metadata = provider.getMetadata();
        if (metadata instanceof LabelMetadataBuilder) {
            ((LabelMetadataBuilder) metadata).setOrigin(resource.toExternalForm());
        }
    }

    private Map<Addon, List<URL>> getAddonWindupXmlFiles() {
        Map<Addon, List<URL>> addon = scanner.scanForAddonMap(new FileExtensionFilter(XML_LABELS_WINDUP_EXTENSION));
        addon.putAll(scanner.scanForAddonMap(new FileExtensionFilter(XML_LABELS_RHAMT_EXTENSION)));
        addon.putAll(scanner.scanForAddonMap(new FileExtensionFilter(XML_LABELS_MTA_EXTENSION)));
        return addon;
    }

    /**
     * Return a collection of all files which might contain custom labels
     */
    private Collection<URL> getWindupUserDirectoryXmlFiles(Path userLabelsPath) {
        // no user dir, so just return the ones that we found in the classpath
        if (userLabelsPath == null)
            return Collections.emptyList();

        try {
            // Deal with the case of a single file here
            if (Files.isRegularFile(userLabelsPath) && pathMatchesNamePattern(userLabelsPath))
                return Collections.singletonList(userLabelsPath.toUri().toURL());

            if (!Files.isDirectory(userLabelsPath)) {
                LOG.warning("Not scanning: " + userLabelsPath.normalize().toString() + " for labels as the directory could not be read!");
                return Collections.emptyList();
            }

            // create the results as a copy (as we will be adding user xml files to them)
            final List<URL> results = new ArrayList<>();

            Files.walkFileTree(userLabelsPath, new SimpleFileVisitor<Path>() {
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
            throw new WindupException("Failed to search userdir: \"" + userLabelsPath + "\" for XML labels due to: "
                    + e.getMessage(), e);
        }
    }

    private boolean pathMatchesNamePattern(Path file) {
        return file.getFileName().toString().toLowerCase().endsWith("." + XML_LABELS_WINDUP_EXTENSION)
                || file.getFileName().toString().toLowerCase().endsWith("." + XML_LABELS_RHAMT_EXTENSION)
                || file.getFileName().toString().toLowerCase().endsWith("." + XML_LABELS_MTA_EXTENSION);
    }

}
