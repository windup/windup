package org.jboss.windup.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.event.PostStartup;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.furnace.FurnaceHolder;
import org.jboss.windup.util.PathUtil;

@Singleton
public class RulesetUpdateChecker
{

    private static final String META_INF_VERSION_KEY = "version=";
    public static final String RULESET_CORE_DIRECTORY = "migration-core";

    public void perform(@Observes PostStartup event)
    {
        boolean isDependencies = event.getAddon().getId().toString().contains("org.jboss.windup.ui:windup-ui");
        if(isDependencies) {
            Furnace furnace = FurnaceHolder.getFurnace();
            Imported<DependencyResolver> exportedTypes = furnace.getAddonRegistry().getServices(DependencyResolver.class);
            if(!exportedTypes.isUnsatisfied() && rulesetNeedUpdate(exportedTypes.get())) {
                    System.out.println("");
                    System.out.println("Your ruleset is obsolete. Consider running windup-update-ruleset command. Press ENTER to continue.");
                    System.out.println("");
            }
            
        }
    }

    public static boolean rulesetNeedUpdate(DependencyResolver dependencyResolver)
    {
        List<Coordinate> resolveVersions = dependencyResolver.resolveVersions(DependencyQueryBuilder.create(CoordinateBuilder.create()
                    .setGroupId("org.jboss.windup.rules")
                    .setArtifactId("windup-rulesets")));
        Coordinate latestCoordinate = resolveVersions.get(resolveVersions.size() - 1);
        Path windupRulesDir = PathUtil.getWindupRulesDir();
        Path coreRulesPropertiesPath = windupRulesDir.resolve(RULESET_CORE_DIRECTORY
                    + "/META-INF/maven/org.jboss.windup.rules/windup-rulesets/pom.properties");
        try (BufferedReader br = Files.newBufferedReader(coreRulesPropertiesPath, StandardCharsets.UTF_8))
        {
            String line;

            while ((line = br.readLine()) != null)
            {
                if (line.startsWith(META_INF_VERSION_KEY))
                {
                    String installedVersion = line.substring(META_INF_VERSION_KEY.length());
                    String latestVersion = latestCoordinate.getVersion();
                    int installedVersionLength = installedVersion.length();
                    // TODO: Maybe there is some other library that can be used here
                    for (int i = 0; i < latestVersion.length(); i++)
                    {
                        if (installedVersionLength > i)
                        {
                            char installedVersionChar = installedVersion.charAt(i);
                            char latestVersionChar = latestVersion.charAt(i);
                            if (Character.isDigit(latestVersionChar))
                            {
                                if (Character.isDigit(installedVersionChar))
                                {
                                    int installedVersionInt = Character.getNumericValue(installedVersionChar);
                                    int latestVersionInt = Character.getNumericValue(latestVersionChar);
                                    if (installedVersionInt < latestVersionInt)
                                    {
                                        return true;
                                    }
                                }
                                else
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;

    }
}
