package org.jboss.windup.reporting.integration.forge;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.windup.metadata.type.FileMetadata;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

import java.io.IOException;


/**
 * Generate new bean interface and implementation object.
 * <p/>
 * User: rsearls
 * Date: 8/22/13
 */
public class JavaFileProcessor implements FileProcessor {
    private static final Log LOG = LogFactory.getLog(JavaFileProcessor.class);

    private SwitchyardForgeRegistrar switchyardForgeRegistrar;

    public JavaFileProcessor(SwitchyardForgeRegistrar switchyardForgeRegistrar) {
        this.switchyardForgeRegistrar = switchyardForgeRegistrar;
    }

    @Override
    public void process(FileMetadata entry, String archiveName) {

        if (archiveName == null || archiveName.trim().length() == 0) {
            LOG.error("archiveName can not be null or blank.");
            throw new IllegalArgumentException("archiveName can not be null or blank.");
        }

        try {

            Project project = switchyardForgeRegistrar.getProject();
            if (project == null) {
                LOG.warn("No project found for archive named: " + archiveName);
            } else {

                LOG.debug("Project created: " + project);

                MetadataFacet mdf = project.getFacet(MetadataFacet.class);
                mdf.setProjectName(archiveName);

                //- adds project to SwitchYardFacet
                FacetFactory facetFactory = switchyardForgeRegistrar.getFacetFactory();
                facetFactory.install(project, SwitchYardFacet.class);

                JavaSource javaSource = JavaParser.parse(FileUtils.readFileToString(
                    entry.getFilePointer()).toCharArray());

                BeanFacet beanFacet = facetFactory.create(project, BeanFacet.class);
                beanFacet.install();
                BeanServiceConfigurator beanServiceConfigurator = new BeanServiceConfigurator();
                beanServiceConfigurator.newBean(project, javaSource.getPackage(),
                    javaSource.getName());

                switchyardForgeRegistrar.getSwitchYardConfigurator().createServiceTest(
                    project, javaSource.getName(), javaSource.getPackage());
            }
        } catch (IOException ioe) {
            LOG.error("Exception getting package name", ioe);
            return;
        }
    }
}
