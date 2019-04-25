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
import java.nio.file.Path;
import java.util.*;

public class XMLLabelLoader implements LabelLoader {
    private static final String XML_EXTENSION = ".windup.label.xml";

    @Inject
    private Furnace furnace;

    @Override
    public Collection<Label> loadLabels(RuleLoaderContext ruleLoaderContext) {
        Set<Label> labels = new HashSet<>();

        for (Path userRulesPath : ruleLoaderContext.getRulePaths()) {
            Visitor<File> visitor = new Visitor<File>() {
                @Override
                public void visit(File file) {
                    labels.addAll(loadTransformers(file));
                }
            };

            FileVisit.visit(userRulesPath.toFile(), new FileSuffixPredicate(XML_EXTENSION), visitor);
        }

        return labels;
    }

    private Set<Label> loadTransformers(File file) {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(file.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);

        parser.setXmlInputPath(file.toPath());
        parser.setXmlInputRootPath(file.getParentFile().toPath());

        return parser.processDocument(file.toURI());
    }
}
