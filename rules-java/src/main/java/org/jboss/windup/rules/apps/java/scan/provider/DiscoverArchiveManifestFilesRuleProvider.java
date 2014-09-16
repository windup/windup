package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.ArchiveService;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers MANIFEST.MF files within archives.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class DiscoverArchiveManifestFilesRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = Logger.getLogger(DiscoverArchiveManifestFilesRuleProvider.class.getSimpleName());

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        ConditionBuilder archivesFound = Query.find(ArchiveModel.class);

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(archivesFound)
                    .perform(new ExtractManifestInformationFromArchive());
    }

    private class ExtractManifestInformationFromArchive extends AbstractIterationOperation<ArchiveModel>
    {
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

            JarManifestService jarManifestService = new JarManifestService(event.getGraphContext());
            JarManifestModel jarManifest = jarManifestService.addTypeToModel(manifestFile);
            jarManifest.setArchive(payload);

            try (InputStream is = manifestFile.asInputStream())
            {
                Manifest manifest = new Manifest(is);
                if (manifest == null || manifest.getMainAttributes().size() == 0)
                {
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

}
