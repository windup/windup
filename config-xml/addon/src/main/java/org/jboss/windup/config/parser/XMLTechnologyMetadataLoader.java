package org.jboss.windup.config.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.TechnologyMetadata;
import org.jboss.windup.config.metadata.TechnologyMetadataLoader;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class XMLTechnologyMetadataLoader implements TechnologyMetadataLoader {
    private static final String XML_EXTENSION = "\\.technology\\.metadata\\.xml";

    @Inject
    private Furnace furnace;

    private List<TechnologyMetadata> metadataList;

    private void load(final GraphContext context) {
        this.metadataList = new ArrayList<>();
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);
        for (FileModel userRulesFileModel : cfg.getUserRulesPaths()) {
            Visitor<File> visitor = new Visitor<File>() {
                @Override
                public void visit(File file) {
                    loadMetadata(file);
                }
            };

            FileVisit.visit(userRulesFileModel.asFile(), new FileSuffixPredicate(XML_EXTENSION), visitor);
        }
    }

    private void loadMetadata(File file) {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(file.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);

        parser.setXmlInputPath(file.toPath());
        parser.setXmlInputRootPath(file.getParentFile().toPath());

        TechnologyMetadata metadata = parser.processDocument(file.toURI());
        metadataList.add(metadata);
    }

    private void loadMetadataIfNeeded(GraphContext context) {
        if (this.metadataList == null) {
            synchronized (this) {
                if (this.metadataList == null)
                    load(context);
            }
        }
    }

    @Override
    public TechnologyMetadata getMetadata(GraphContext context, TechnologyReference reference) {
        loadMetadataIfNeeded(context);

        for (TechnologyMetadata metadata : metadataList) {
            if (metadata.handles(reference))
                return metadata;
        }
        return null;
    }
}
