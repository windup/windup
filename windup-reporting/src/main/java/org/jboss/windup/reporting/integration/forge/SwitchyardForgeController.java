package org.jboss.windup.reporting.integration.forge;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.manager.AddonManager;
import org.jboss.forge.addon.manager.impl.AddonManagerImpl;
import org.jboss.forge.addon.manager.request.InstallRequest;
import org.jboss.forge.addon.manager.spi.AddonDependencyResolver;
import org.jboss.forge.addon.maven.addon.MavenAddonDependencyResolver;
import org.jboss.forge.addon.maven.dependencies.MavenDependencyResolver;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourceFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.repositories.AddonRepositoryMode;
import org.jboss.forge.furnace.se.FurnaceFactory;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

public class SwitchyardForgeController implements Reporter
{

   private static final Log LOG = LogFactory.getLog(SwitchyardForgeController.class);
   {
      System.setProperty("modules.ignore.jdk.factory", "true");
   }

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

            install(furnace, "org.jboss.forge.addon:parser-java,2.0.0-SNAPSHOT");
            install(furnace, "org.jboss.forge.addon:projects,2.0.0-SNAPSHOT");
            install(furnace, "org.jboss.forge.addon:maven,2.0.0-SNAPSHOT");

            AddonRegistry registry = furnace.getAddonRegistry();
            Addons.waitUntilStarted(registry.getAddon(AddonId.from("org.jboss.forge.addon:projects", "2.0.0-SNAPSHOT")));

            ResourceFactory resourceFactory = registry.getExportedInstance(ResourceFactory.class).get();
            ProjectFactory projectFactory = registry.getExportedInstance(ProjectFactory.class).get();

            DirectoryResource dr = resourceFactory.create(DirectoryResource.class, forgeOutput);
            DirectoryResource projectDir = dr.getChildDirectory("project");
            projectDir.mkdir();

            List<Class<? extends ProjectFacet>> facetsToInstall = Arrays.asList(JavaSourceFacet.class,
                     ResourceFacet.class);
            Project project = projectFactory.createProject(projectDir, facetsToInstall);
            if (project != null)
            {
               LOG.info("Project created: " + project);
               project.getFacet(JavaSourceFacet.class).saveJavaSource(
                        JavaParser.create(JavaClass.class).setPackage("com.example").setName("ExampleClass"));
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

   private void install(Furnace furnace, String addonCoordinates)
   {
      try
      {
         MavenDependencyResolver resolver = new MavenDependencyResolver();
         AddonDependencyResolver addonResolver = new MavenAddonDependencyResolver();
         AddonManager addonManager = new AddonManagerImpl(furnace, addonResolver, false);

         AddonId addon;
         // This allows forge --install maven
         if (addonCoordinates.contains(","))
         {
            addon = AddonId.fromCoordinates(addonCoordinates);
         }
         else
         {
            String coordinates = "org.jboss.forge.addon:" + addonCoordinates;
            CoordinateBuilder coordinate = CoordinateBuilder.create(coordinates);
            List<Coordinate> versions = resolver.resolveVersions(DependencyQueryBuilder.create(coordinate));
            if (versions.isEmpty())
            {
               throw new IllegalArgumentException("No Artifact version found for " + coordinate);
            }
            Coordinate vCoord = versions.get(versions.size() - 1);
            addon = AddonId.from(vCoord.getGroupId() + ":" + vCoord.getArtifactId(), vCoord.getVersion());
         }

         InstallRequest request = addonManager.install(addon);
         System.out.println(request);
         request.perform();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
