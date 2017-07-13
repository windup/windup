package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.HasManifestFilesModel;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers MANIFEST.MF files within archives.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ArchiveMetadataExtractionPhase.class, perform = "DiscoverManifestFilesInArchives")
public class DiscoverArchiveManifestFilesRuleProvider extends IteratingRuleProvider<ArchiveModel>
{
    private static final Logger LOG = Logging.get(DiscoverArchiveManifestFilesRuleProvider.class);

    private static final String TECH_TAG = "Manifest";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(ArchiveModel.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        String[] filenames = {
                    "META-INF/MANIFEST.MF",
                    "WEB-INF/classes/META-INF/MANIFEST.MF"
        };
        Arrays.stream(filenames).forEach(filename -> {
            importManifest(event, payload, filename);
        });
    }

    private void importManifest(GraphRewrite event, ArchiveModel archive, String manifestFilePath)
    {
        ArchiveService archiveService = new ArchiveService(event.getGraphContext());
        FileModel manifestFile = archiveService.getChildFile(archive, manifestFilePath);

        if (manifestFile == null)
        {
            // no manifest found, skip this one
            return;
        }
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        JarManifestService jarManifestService = new JarManifestService(event.getGraphContext());
        technologyTagService.addTagToFileModel(manifestFile, TECH_TAG, TECH_TAG_LEVEL);

        JarManifestModel jarManifest = jarManifestService.addTypeToModel(manifestFile);
        GraphService<HasManifestFilesModel> hasManifestFilesModelService = new GraphService<>(event.getGraphContext(), HasManifestFilesModel.class);
        hasManifestFilesModelService.addTypeToModel(archive).addManifestModel(jarManifest);

        jarManifest.setGenerateSourceReport(true);

        try (InputStream is = manifestFile.asInputStream())
        {
            Manifest manifest = new Manifest(is);
            if (manifest.getMainAttributes().isEmpty())
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

            if (StringUtils.isBlank(jarManifest.getName()))
            {
                // if the name is still blank, try to get it from the first entry in the file list.
                // A few apache projects do it this way
                for (String entry : manifest.getEntries().keySet())
                {
                    for (Object key : manifest.getAttributes(entry).keySet())
                    {
                        String property = StringUtils.trim(key.toString());
                        String propertyValue = StringUtils.trim(manifest.getAttributes(entry).get(key).toString());
                        if (StringUtils.isBlank((String) jarManifest.asVertex().getProperty(property)))
                            jarManifest.asVertex().setProperty(property, propertyValue);
                    }
                    if (!StringUtils.isBlank(jarManifest.getName()))
                        break;
                }
            }
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING, "Exception reading manifest from file: " + manifestFile.getFilePath(), e);
        }
    }
}
