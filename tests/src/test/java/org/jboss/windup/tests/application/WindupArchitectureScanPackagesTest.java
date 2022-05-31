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

    @Inject
    private CompositeArchiveIdentificationService identifier;

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

    @Ignore
    @Test
    public void testRunWindupScanPackages() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";
        final String includedPackage = "org.apache.wicket.application";

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, path, null, false, Collections.singletonList(includedPackage), Collections.emptyList());

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
            super.runTest(context, path, null, false, Collections.singletonList(includedPackage), Collections.singletonList(excludedPackage));

            validateInlineHintsInAppropriatePackages(context, includedPackage, excludedPackage);
        }
    }

    @Test
    public void testRunWindupExcludesPomsWhenAnalyzeKnownLibrariesIsFalse() throws Exception {
        final String path = "../test-files/Windup1x-javaee-example.war";

        InMemoryArchiveIdentificationService testIdService = new InMemoryArchiveIdentificationService();
        testIdService.addMapping("6f3b8a24bf970f17289b234284c94f43eb42f0e4", "org.slf4j:slf4j-api");
        testIdService.addMapping("43472749d5856b6c568eb52690b8a85c738988b1", "org.apache.wicket:wicket-extensions");
        testIdService.addMapping("66859922767bb787a7ff4c685c74b5b28cc55909", "org.apache.wicket:wicket-datetime");
        testIdService.addMapping("7cbbf3bb2c2442885836ff948e42ccc093c64dd6", "org.apache.wicket:wicket-util");
        testIdService.addMapping("752c0fe8a5b2d6704c9d9d6f06d9abdcacf4045d", "joda-time:joda-time");
        testIdService.addMapping("736e42799a54762d47f7061d7d6ca50fd4543f6d", "org.apache.wicket:wicket-core");
        testIdService.addMapping("898c997df6d4b2d35df3d042641e2148c7fb5d33", "org.apache.wicket:wicket-devutils");
        testIdService.addMapping("ff3e0312fb474eb84a88aa3039d1c414be51f85e", "org.apache.wicket:wicket-request");
        identifier.addIdentifier(testIdService);

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, Collections.singletonList(path), null, false,
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
