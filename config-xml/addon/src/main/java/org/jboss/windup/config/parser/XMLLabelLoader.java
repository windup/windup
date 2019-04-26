package org.jboss.windup.config.parser;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.metadata.LabelLoader;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
public class XMLLabelLoader implements LabelLoader
{
    private static final String XML_WINDUP_EXTENSION = ".windup.label.xml";

    @Inject
    private Furnace furnace;

    @Override
    public Collection<Label> loadLabels(RuleLoaderContext ruleLoaderContext)
    {
        Set<Label> labels = new HashSet<>();

        for (Path userRulesPath : ruleLoaderContext.getRulePaths())
        {
            // Deal with the case of a single file here
            if (Files.isRegularFile(userRulesPath) && pathMatchesNamePattern(userRulesPath)) {
                labels.addAll(loadTransformers(userRulesPath.toFile()));
            } else {
                Visitor<File> visitor = file -> labels.addAll(loadTransformers(file));
                FileVisit.visit(userRulesPath.toFile(), new FileSuffixPredicate(XML_WINDUP_EXTENSION), visitor);
            }
        }

        return labels;
    }

    private Set<Label> loadTransformers(File file)
    {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(file.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);

        parser.setXmlInputPath(file.toPath());
        parser.setXmlInputRootPath(file.getParentFile().toPath());

        return parser.processDocument(file.toURI());
    }

    private boolean pathMatchesNamePattern(Path file)
    {
        return file.getFileName().toString().toLowerCase().endsWith(XML_WINDUP_EXTENSION);
    }
}
