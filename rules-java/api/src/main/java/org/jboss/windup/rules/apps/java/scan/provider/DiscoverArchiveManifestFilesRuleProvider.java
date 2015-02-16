package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.phase.ArchiveMetadataExtraction;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers MANIFEST.MF files within archives.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class DiscoverArchiveManifestFilesRuleProvider extends IteratingRuleProvider<ArchiveModel>
{
    private static final Logger LOG = Logger.getLogger(DiscoverArchiveManifestFilesRuleProvider.class.getSimpleName());

    private static final String TECH_TAG = "Manifest";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.IMPORTANT;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ArchiveMetadataExtraction.class;
    }

    @Override
    public String toStringPerform()
    {
        return "DiscoverManifestFilesInArchives";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(ArchiveModel.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        ArchiveService archiveService = new ArchiveService(event.getGraphContext());
        FileModel manifestFile = archiveService.getChildFile(payload, "META-INF/MANIFEST.MF");
        if (manifestFile == null)
        {
            // no manifest found, skip this one
            return;
        }
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        JarManifestService jarManifestService = new JarManifestService(event.getGraphContext());
        technologyTagService.addTagToFileModel(manifestFile, TECH_TAG, TECH_TAG_LEVEL);

        JarManifestModel jarManifest = jarManifestService.addTypeToModel(manifestFile);
        jarManifest.setArchive(payload);

        try (InputStream is = manifestFile.asInputStream())
        {
            Manifest manifest = new Manifest(is);
            if (manifest == null || manifest.getMainAttributes().size() == 0)
            {
                // no manifest found, skip this one
                return;
            }

            for (Object key : manifest.getMainAttributes().keySet())
            {
                String property = StringUtils.trim(key.toString());
                String propertyValue = StringUtils.trim(manifest.getMainAttributes().get(key).toString());
                jarManifest.asVertex().setProperty(property, propertyValue);
            }
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING, "Exception reading manifest from file: " + manifestFile.getFilePath(), e);
        }
    }

}
