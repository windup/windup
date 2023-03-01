package org.jboss.windup.tests.application.newreports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.data.dto.ApplicationDetailsDto;
import org.jboss.windup.reporting.data.dto.FileDto;
import org.jboss.windup.reporting.data.rules.ApplicationDetailsRuleProvider;
import org.jboss.windup.reporting.data.rules.FilesRuleProvider;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.tests.application.WindupArchitectureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class NewReports_WindupArchitectureCatchallTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting-data"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(WindupArchitectureTest.class);
    }

    @Inject
    private JspRulesProvider provider;

    @Test
    public void testRunWindupJsp() throws Exception {
        final String path = "../test-files/catchalltest";

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, false, path, true);
            validateReports(context);
        }
    }

    private void validateReports(GraphContext context) throws IOException {
        File applicationDetailsJson = new ReportService(context).getApiDataDirectory().resolve(ApplicationDetailsRuleProvider.PATH + ".json").toFile();
        File filesJson = new ReportService(context).getApiDataDirectory().resolve(FilesRuleProvider.PATH + ".json").toFile();

        ApplicationDetailsDto[] appDetailsDtoList = new ObjectMapper().readValue(applicationDetailsJson, ApplicationDetailsDto[].class);
        Assert.assertEquals(1, appDetailsDtoList.length);

        FileDto[] filesDtoList = new ObjectMapper().readValue(filesJson, FileDto[].class);
        Assert.assertTrue(filesDtoList.length > 1);

        Optional<ApplicationDetailsDto.ApplicationFileDto> catchalltest = appDetailsDtoList[0].getApplicationFiles().stream()
                .filter(dto -> dto.getFileName().equals("catchalltest"))
                .findFirst();
        Assert.assertTrue(catchalltest.isPresent());

        List<FileDto> filesDtoCollection = Arrays.asList(filesDtoList);
        List<FileDto> childrenFiles = catchalltest.get().getChildrenFileIds().stream()
                .map(childFileId -> filesDtoCollection.stream()
                        .filter(fileDto -> fileDto.getId().equals(childFileId)).findFirst().orElse(null)
                ).collect(Collectors.toList());

        boolean fileWithoutCatchallHits_storyPointsMatch = childrenFiles.stream()
                .filter(fileDto -> fileDto.getPrettyFileName().equals("FileWithoutCatchallHits"))
                .allMatch(fileDto -> fileDto.getStoryPoints() == 13);
        boolean fileWithBoth_storyPointsMatch = childrenFiles.stream()
                .filter(fileDto -> fileDto.getPrettyFileName().equals("FileWithBoth"))
                .allMatch(fileDto -> fileDto.getStoryPoints() == 27);
        boolean fileWithNoHintsRules_storyPointsMatch = childrenFiles.stream()
                .filter(fileDto -> fileDto.getPrettyFileName().equals("FileWithNoHintsRules"))
                .allMatch(fileDto -> fileDto.getStoryPoints() == 63);

        Assert.assertTrue(fileWithoutCatchallHits_storyPointsMatch);
        Assert.assertTrue(fileWithBoth_storyPointsMatch);
        Assert.assertTrue(fileWithNoHintsRules_storyPointsMatch);
    }

    @Singleton
    public static class JspRulesProvider extends AbstractRuleProvider {
        public JspRulesProvider() {
            super(MetadataBuilder.forProvider(JspRulesProvider.class)
                    .setHaltOnException(true));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            Set<String> catchallTags = Collections.singleton("catchall");
            Set<String> otherTags = new HashSet<>();
            otherTags.add("tag1");
            otherTags.add("tag2");
            otherTags.add("tag3");

            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("java.util.{*}"))
                    .perform(Hint.titled("java.util.* found").withText("Catchall hint is here").withEffort(7).withTags(catchallTags))

                    .addRule()
                    .when(JavaClass.references("java.net.URL"))
                    .perform(Hint.titled("java.net.URL").withText("Java Net URL is here (no catchall").withEffort(13).withTags(otherTags))

                    .addRule()
                    .when(JavaClass.references("java.util.HashMap"))
                    .perform(Hint.titled("java.util.HashMap").withText("Java Net URL is here (no catchall").withEffort(42));
        }
    }

}
