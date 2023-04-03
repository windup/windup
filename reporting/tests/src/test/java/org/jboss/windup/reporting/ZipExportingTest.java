package org.jboss.windup.reporting;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.LegacyReportsRenderingOption;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.SkipReportsRenderingOption;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.export.ExportZipReportRuleProvider;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ZipExportingTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private GraphContextFactory factory;

    @Inject
    private WindupProcessor processor;

    @Test
    public void testExportZipReport() throws Exception {
        zipReportTest(true, false, false);
    }

    @Test
    public void testExportZipReportLegacyReports() throws Exception {
        zipReportTest(true, true, false);
    }

    @Test
    public void testExportZipReportSkipReports() throws Exception {
        zipReportTest(true, false, true);
    }

    @Test
    public void testNotExportZipReport() throws Exception {
        zipReportTest(false, false, false);
    }

    private void zipReportTest(boolean exportZipReports, boolean legacyReports, boolean skipReports) throws Exception {
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                "windup_" + RandomStringUtils.randomAlphanumeric(6));

        outputPath.toFile().mkdirs();
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources";
            Predicate<RuleProvider> predicate = new RuleProviderWithDependenciesPredicate(ExportZipReportRuleProvider.class);
            WindupConfiguration configuration = new WindupConfiguration()
                    .setGraphContext(context)
                    .setRuleProviderFilter(predicate)
                    .addInputPath(Paths.get(inputPath))
                    .setOutputDirectory(outputPath)
                    .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                    .setOptionValue(SourceModeOption.NAME, true);
            if (exportZipReports) {
                configuration.setExportingZipReport(true);
                configuration.setOptionValue(LegacyReportsRenderingOption.NAME, legacyReports);
                if (skipReports) configuration.setOptionValue(SkipReportsRenderingOption.NAME, true);
            }
            processor.execute(configuration);
            final File reportsZipFile = new File(outputPath + "/reports.zip");
            Assert.assertEquals(exportZipReports, reportsZipFile.exists());
            if (exportZipReports) {
                try (ZipFile reportsZip = new ZipFile(reportsZipFile);
                     Stream<Path> outputFiles = Files.walk(outputPath.toAbsolutePath())){
                    final Set<String> zipContent = reportsZip.stream()
                            .map(ZipEntry::getName)
                            .sorted()
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    Assert.assertTrue(
                            "Zip file content is different from the output folder content",
                            outputFiles
                                    // reports.zip file itself and root folder are not part of the ZIP file
                                    .filter(file -> !file.endsWith("reports.zip") && !outputPath.equals(file))
                                    .map(path -> {
                                        final Path relativePath = outputPath.relativize(path);
                                        // in the ZipFile, the directories end with "/" but they don't in the Files.walk
                                        if (Files.isDirectory(path)) {
                                            return relativePath + File.separator;
                                        }
                                        else {
                                            return relativePath.toString();
                                        }
                                    })
                                    // check every file in the output path is contained in the ZIP file
                                    .allMatch(zipContent::contains));
                }
            }
        }
    }
}
