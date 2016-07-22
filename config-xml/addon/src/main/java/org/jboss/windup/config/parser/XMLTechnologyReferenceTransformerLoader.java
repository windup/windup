package org.jboss.windup.config.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.metadata.TechnologyReferenceTransformer;
import org.jboss.windup.config.metadata.TechnologyReferenceTransformerLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;

/**
 * Implements a {@link TechnologyReferenceTransformerLoader} using xml files. The XML files must end with the extension
 * ".windup.technologytransformer.xml".
 *
 * The format of the file is defined by {@link TechnologyReferenceTransformerHandler}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class XMLTechnologyReferenceTransformerLoader implements TechnologyReferenceTransformerLoader
{
    private static final String XML_EXTENSION = ".windup.technologytransformer.xml";

    @Inject
    private Furnace furnace;

    @Override
    public Collection<TechnologyReferenceTransformer> loadTransformers(GraphContext graphContext)
    {
        final List<TechnologyReferenceTransformer> transformers = new ArrayList<>();

        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(graphContext);
        for (FileModel userRulesFileModel : cfg.getUserRulesPaths())
        {
            
            
            Visitor<File> visitor = new Visitor<File>()
            {
                @Override
                public void visit(File file)
                {
                    transformers.addAll(loadTransformers(file));
                }
            };

            FileVisit.visit(userRulesFileModel.asFile(), new FileSuffixPredicate(XML_EXTENSION), visitor);
        }

        return transformers;
    }

    private List<TechnologyReferenceTransformer> loadTransformers(File file)
    {
        ParserContext parser = new ParserContext(furnace);

        parser.setXmlInputPath(file.toPath());
        parser.setXmlInputRootPath(file.getParentFile().toPath());

        return parser.processDocument(file.toURI());
    }
}
