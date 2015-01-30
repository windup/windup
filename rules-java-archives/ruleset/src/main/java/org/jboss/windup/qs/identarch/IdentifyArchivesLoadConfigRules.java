package org.jboss.windup.qs.identarch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import org.jboss.windup.qs.identarch.lib.ArchiveGAVIdentifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.windup.config.GraphRewrite;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.Initialization;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Rules which support skipping certain archives by their G:A:V definition.
 * The purpose is to speed up processing of the scanned deployments.
 * The archive that is defined to be skipped (currently in a bundled text file)
 * is marked in the graph with a "w:skip" property.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class IdentifyArchivesLoadConfigRules extends WindupRuleProvider
{
    private static final Logger log = Logging.get(IdentifyArchivesLoadConfigRules.class);

    public static final String CENTRAL_MAPPING_DATA_CLASSPATH = "/META-INF/data/central.sha1ToGAV.txt.zip";


    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList();
    }


    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Initialization.class;
    }




    // @formatter:off
    @Override
    public Configuration getConfiguration(final GraphContext grCtx)
    {
        return ConfigurationBuilder.begin()

        // Check the jars
        .addRule()
        .perform(
            new GraphOperation()
            {
                public void perform(GraphRewrite event, EvaluationContext evCtx)
                {
                    loadConfig();
                }
            }
        ).withId("IdentifyArchivesLoadConfig");
    }
    // @formatter:on



    private void loadConfig()
    {

        // Load them from ~/.windup/config/IdentifyArchives
        final File confDir = WindupPathUtil.getWindupUserDir().resolve("config/IdentifyArchives").toFile();
        if (!confDir.exists())
            log.info("IdentifyArchives config dir not found at " + confDir.toString());
        else try
        {
            List<Path> gavs = findFilesBySuffix(confDir, ".gavMapping.txt");

            for(Path gavMappingFile : gavs)
                ArchiveGAVIdentifier.addMappingsFrom(confDir.toPath().resolve(gavMappingFile).toFile());
        }
        catch (IOException ex)
        {
            Logger.getLogger(IdentifyArchivesLoadConfigRules.class.getName()).log(Level.SEVERE, null, ex);
        }

        // ====== CONSTRUCTION AREA ======= //

        // GAV's may also be bundled within the IdentArch addon.
        final String GAVS_MAPPING_RESOURCE =
                //ResourceUtils.getResourcesPath(IdentifyArchivesLoadConfigRules.class)
                //CENTRAL_MAPPING_DATA_CLASSPATH;  // -- Trying to load it from target/classes through ShrinkWrap
                //"/x.zip";  // -- Trying to load it form ShrinkWrap
                //"/META-INF/beans.xml"; -- This WORKS :/
                "/central.SHA1toGAVs.sorted.txt"; // -- Trying load it in other Forge addon

        InputStream is2 = FurnaceHolder.getFurnace().getRuntimeClassLoader().getResourceAsStream(GAVS_MAPPING_RESOURCE);
        is2 = FurnaceHolder.getAddonRegistry().getAddon(AddonId.from("org.jboss.windup.quickstarts:windup-skiparch-mappings", "2.0.0-SNAPSHOT")).getClassLoader().getResourceAsStream(GAVS_MAPPING_RESOURCE);

        try(InputStream is = //Thread.currentThread().getContextClassLoader().getResourceAsStream(GAVS_MAPPING_RESOURCE))
                getClass().getResourceAsStream(GAVS_MAPPING_RESOURCE))
        // ====== CONSTRUCTION AREA ======= //

        {
            if (is == null)
                log.info("IdentifyArchives' bundled G:A:V mappings not found at " + GAVS_MAPPING_RESOURCE);
            else
            {
                log.info("IdentifyArchives loading bundled G:A:V mappings from " + GAVS_MAPPING_RESOURCE);
                ArchiveGAVIdentifier.addMappingsFromZip(is);
            }
        }
        catch(IOException ex){} // Ignore ex from .close()
    }


    /**
     * Scans given directory for files with name ending with given suffix.
     *
     * @return  A list of paths to matching files, relative to baseDir.
     */
    private List<Path> findFilesBySuffix(final File baseDir, final String suffix) throws IOException
    {
        final LinkedList<Path> foundSubPaths = new LinkedList();

        final Path basePath = baseDir.toPath();

        new DirectoryWalker<Path>(DirectoryFileFilter.DIRECTORY, new SuffixFileFilter(suffix), -1)
        {
            void findArchives() throws IOException
            {
                this.walk(baseDir, foundSubPaths);
            }

            @Override
            protected void handleFile(File file, int depth, Collection<Path> results) throws IOException
            {
                Path relPath = basePath.relativize(file.toPath());
                results.add(relPath);
            }
        }.findArchives();

        return foundSubPaths;
    }


}
