package org.jboss.windup.reporting.integration.forge;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.manager.AddonManager;
import org.jboss.forge.furnace.manager.impl.AddonManagerImpl;
import org.jboss.forge.furnace.manager.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.furnace.manager.request.InstallRequest;
import org.jboss.forge.furnace.manager.spi.AddonDependencyResolver;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;

import java.io.File;

/**
 * Switchyard-Forge has standard configuration requirements in particular using
 * a single classloader that is provided by Furnace.
 *
 * This class is a central location to access several often used objects provided
 * by the Switchyard-Forge env.
 *
 * The installing of Addons takes a long time.  It is commended to create this
 * class as few times as possible.
 *
 * User: rsearls
 * Date: 8/28/13
 */
public class SwitchyardForgeRegistrar {
    private static final Log LOG = LogFactory.getLog(SwitchyardForgeRegistrar.class);

    private File forgeOutput;
    private ResourceFactory resourceFactory;
    private ProjectFactory projectFactory;
    private FacetFactory facetFactory;
    private Furnace furnace;
    private SwitchYardConfigurator switchYardConfig;
    private Project project;

    public SwitchyardForgeRegistrar(File reportDirectory) {

        forgeOutput = new File(reportDirectory, "forge");
        try {
            furnace = FurnaceFactory.getInstance();
            try {
                FileUtils.forceMkdir(forgeOutput);

                furnace.addRepository(AddonRepositoryMode.MUTABLE,
                    new File(OperatingSystemUtils.getUserHomeDir(), ".windup"));
                furnace.startAsync();

                while (!furnace.getStatus().isStarted()) {
                    LOG.info("FURNACE STATUS: " + furnace.getStatus());
                    Thread.sleep(100);
                }

                AddonId switchyardId = AddonId
                    .fromCoordinates("org.switchyard.forge:switchyard-forge-plugin,1.0.0-SNAPSHOT");
                install(furnace, AddonId.fromCoordinates("org.jboss.forge.addon:parser-java,2.0.0-SNAPSHOT"));
                install(furnace, AddonId.fromCoordinates("org.jboss.forge.addon:projects,2.0.0-SNAPSHOT"));
                install(furnace, AddonId.fromCoordinates("org.jboss.forge.addon:maven,2.0.0-SNAPSHOT"));
                install(furnace, switchyardId);

                AddonRegistry registry = furnace.getAddonRegistry();
                Addons.waitUntilStarted(registry.getAddon(switchyardId));

                /**
                 * Get the necessary factory instances.
                 */
                resourceFactory = registry.getServices(ResourceFactory.class).get();
                projectFactory = registry.getServices(ProjectFactory.class).get();
                facetFactory = registry.getServices(FacetFactory.class).get();

            } catch (Exception e) {
                LOG.error(e);
                this.stop();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        this.furnace.stop();
        LOG.info("Furnace stopped.");
    }

    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    public FacetFactory getFacetFactory() {
        return facetFactory;
    }

    public SwitchYardConfigurator getSwitchYardConfigurator(){
        if(switchYardConfig == null){
            this.switchYardConfig = furnace.getAddonRegistry().getServices(
                SwitchYardConfigurator.class).get();
        }
        return this.switchYardConfig;
    }

    public Project createProject(String name){
        DirectoryResource dr = resourceFactory.create(DirectoryResource.class, forgeOutput);
        DirectoryResource projectDir = dr.getChildDirectory(name);
        projectDir.mkdir();

        this.project = projectFactory.createProject(projectDir);
        return this.project;
    }

    public Project getProject(){
        return this.project;
    }

    private static void install(Furnace furnace, AddonId addonId) {
        try {
            AddonDependencyResolver addonResolver = new MavenAddonDependencyResolver();
            AddonManager addonManager = new AddonManagerImpl(furnace, addonResolver, false);

            InstallRequest request = addonManager.install(addonId);
            System.out.println(request);
            request.perform();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
