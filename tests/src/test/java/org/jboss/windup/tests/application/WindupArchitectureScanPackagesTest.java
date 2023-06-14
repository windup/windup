package org.jboss.windup.tests.application;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.MigrationIssuesReportModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.testutil.html.TestMigrationIssuesReportUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.Map;

@RunWith(Arquillian.class)
public class WindupArchitectureScanPackagesTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class)
                .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Inject
    private CompositeArchiveIdentificationService identifier;

    @Ignore
    @Test
    public void testRunWindupScanPackages() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";
        final String includedPackage = "org.apache.wicket.application";

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, true, path, null, false, Collections.singletonList(includedPackage), Collections.emptyList());

            validateInlineHintsInAppropriatePackages(context, includedPackage, "");
        }
    }

    @Ignore
    @Test
    public void testRunWindupExcludePackages() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";
        final String excludedPackage = "org.slf4j";
        final String includedPackage = "org.apache.wicket.application";

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, true, path, null, false, Collections.singletonList(includedPackage), Collections.singletonList(excludedPackage));

            validateInlineHintsInAppropriatePackages(context, includedPackage, excludedPackage);
        }
    }

    @Test
    public void testRunWindupExcludesPomsWhenAnalyzeKnownLibrariesIsFalse() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";

        InMemoryArchiveIdentificationService testIdService = new InMemoryArchiveIdentificationService();
        testIdService.addMapping("df9f9ab084ac2b7527fba5038085db6b10f37a18", "org.slf4j:slf4j-api");
        testIdService.addMapping("2450f1316a85bbc2ce71de72cc4dd4d934ebd034", "org.apache.wicket:wicket-extensions");
        testIdService.addMapping("16a6b409be0c259c91aaf4cf5f935fb91926effb", "org.apache.wicket:wicket-datetime");
        testIdService.addMapping("16afb17a9403ebde1e3f8ab606662e3479891a40", "org.apache.wicket:wicket-util");
        testIdService.addMapping("c19885d06f26586b81dea43d27d092bb0082168a", "joda-time:joda-time");
        testIdService.addMapping("48691652416413a4f3a66de72ccd0bedd18f2e40", "org.apache.wicket:wicket-core");
        testIdService.addMapping("fa1d62a2dcba6fce2cc60199aad642f40ef2e3dc", "org.apache.wicket:wicket-devutils");
        testIdService.addMapping("768f4a0acc6567f912f3d7de4418df2466b96cdc", "org.apache.wicket:wicket-request");
        identifier.addIdentifier(testIdService);

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, true, Collections.singletonList(path), null, false,
                    Collections.emptyList(), Collections.emptyList(), Map.of("analyzeKnownLibraries", false));

            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
            ProjectModel project = null;
            for (FileModel inputFile : configurationModel.getInputPaths()) {
                if (inputFile.getFileName().equals("Windup1x-javaee-example.war"))
                    project = inputFile.getProjectModel();
            }

            MigrationIssuesReportModel mainIssuesReportModel = getMigrationIssuesReport(context, project);
            TestMigrationIssuesReportUtil migrationIssuesReportUtil = new TestMigrationIssuesReportUtil();
            migrationIssuesReportUtil.loadPage(getPathForReport(context, mainIssuesReportModel));

            Assert.assertTrue(migrationIssuesReportUtil.checkIssue("Maven POM (pom.xml)", 1, 0, "Info", 0));
        }
    }

    private void validateInlineHintsInAppropriatePackages(GraphContext context, String includedPackage, String excludedPackage) {
        GraphService<FileModel> fileModelService = new GraphService<>(context, FileModel.class);
        boolean foundHintedFile = false;
        boolean foundAppHintedFile = false;
        boolean foundNonAppHintedFile = false;

        InlineHintService inlineHintService = new InlineHintService(context);

        for (FileModel fileModel : fileModelService.findAll()) {
            String pkg = null;
            if (fileModel instanceof JavaClassFileModel) {
                pkg = ((JavaClassFileModel) fileModel).getPackageName();
            } else if (fileModel instanceof JavaSourceFileModel) {
                pkg = ((JavaSourceFileModel) fileModel).getPackageName();
            }

            if (pkg == null) {
                continue;
            }
            Iterable<InlineHintModel> hintIterable = inlineHintService.getHintsForFile(fileModel);
            for (InlineHintModel hint : hintIterable) {
                foundHintedFile = true;
                if (pkg.startsWith(includedPackage)) {
                    foundAppHintedFile = true;
                } else if (pkg.startsWith(excludedPackage)) {
                    foundNonAppHintedFile = true;
                } else {
                    System.out.println("Unexpected hinted file found: " + fileModel.getFilePath() + " hint: " + hint.getTitle() + " desc: "
                            + hint.getDescription());
                    foundNonAppHintedFile = true;
                }
            }
        }

        Assert.assertTrue(foundHintedFile);
        Assert.assertTrue(foundAppHintedFile);
        Assert.assertFalse(foundNonAppHintedFile);
    }

}
