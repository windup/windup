package org.jboss.windup.config.parser;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.TechnologyReferenceAliasTranslator;
import org.jboss.windup.config.metadata.TechnologyReferenceAliasTranslatorLoader;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Implements a {@link TechnologyReferenceAliasTranslatorLoader} using xml files. The XML files must end with the extension
 * ".windup.technologytransformer.xml".
 *
 * The format of the file is defined by {@link TechnologyReferenceTransformerHandler}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class XMLTechnologyReferenceAliasTranslatorLoader implements TechnologyReferenceAliasTranslatorLoader
{
    private static final String XML_EXTENSION = ".windup.technologytransformer.xml";

    @Inject
    private Furnace furnace;

    @Override
    public Collection<TechnologyReferenceAliasTranslator> loadTranslators(RuleLoaderContext ruleLoaderContext)
    {
        List<TechnologyReferenceAliasTranslator> transformers = new ArrayList<>();

        for (Path userRulesPath : ruleLoaderContext.getRulePaths())
        {
            Visitor<File> visitor = new Visitor<File>() {
                @Override
                public void visit(File file) {
                    transformers.addAll(loadTransformers(file));
                }
            };

            FileVisit.visit(userRulesPath.toFile(), new FileSuffixPredicate(XML_EXTENSION), visitor);
        }

        return transformers;
    }

    private List<TechnologyReferenceAliasTranslator> loadTransformers(File file)
    {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(file.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);

        parser.setXmlInputPath(file.toPath());
        parser.setXmlInputRootPath(file.getParentFile().toPath());

        return parser.processDocument(file.toURI());
    }
}
