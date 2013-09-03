package org.jboss.windup.reporting.integration.forge;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
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
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

public class SwitchyardForgeController implements Reporter
{
   private static final Log LOG = LogFactory.getLog(SwitchyardForgeController.class);

   @Override
   @SuppressWarnings("unchecked")
   public void process(ArchiveMetadata archive, File reportDirectory)
   {
      File forgeOutput = new File(reportDirectory, "forge");
      try
      {
         final Furnace furnace = FurnaceFactory.getInstance();
         try
         {
            FileUtils.forceMkdir(forgeOutput);

            furnace.addRepository(AddonRepositoryMode.MUTABLE, new File(OperatingSystemUtils.getUserHomeDir(),
                     ".windup"));
            furnace.startAsync();

            while (!furnace.getStatus().isStarted())
            {
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
            ResourceFactory resourceFactory = registry.getServices(ResourceFactory.class).get();
            ProjectFactory projectFactory = registry.getServices(ProjectFactory.class).get();
            FacetFactory facetFactory = registry.getServices(FacetFactory.class).get();

            DirectoryResource dr = resourceFactory.create(DirectoryResource.class, forgeOutput);
            DirectoryResource projectDir = dr.getChildDirectory(archive.getName());
            projectDir.mkdir();

            List<Class<? extends ProjectFacet>> facetsToInstall = Arrays.asList(JavaSourceFacet.class,
                     ResourcesFacet.class);
            Project project = projectFactory.createProject(projectDir, facetsToInstall);
            if (project != null)
            {

               LOG.info("Project created: " + project);
               project.getFacet(JavaSourceFacet.class).saveJavaSource(
                        JavaParser.create(JavaClass.class).setPackage("com.example").setName("ExampleClass"));
               MetadataFacet mdf = project.getFacet(MetadataFacet.class);
               mdf.setProjectName(archive.getName());

               /**
                * Install and retrieve the switchyard facet
                */
               SwitchYardFacet syf = facetFactory.install(project, SwitchYardFacet.class);

               /**
                * Do more stuff...
                */
            }

         }
         finally
         {
            furnace.stop();
            LOG.info("Furnace stopped.");
         }

      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
   }

   private static void install(Furnace furnace, AddonId addonId)
   {
      try
      {
         AddonDependencyResolver addonResolver = new MavenAddonDependencyResolver();
         AddonManager addonManager = new AddonManagerImpl(furnace, addonResolver, false);

         InstallRequest request = addonManager.install(addonId);
         System.out.println(request);
         request.perform();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
