package org.jboss.windup.rules.java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationListCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationLiteralCondition;
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaClassAnnotationFilteringTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    BasicAnnotationScanProvider basicProvider;

    @Inject
    ComplexAnnotationScanProvider complexProvider;

    @Inject
    RegexAnnotationScanProvider regexProvider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testBasicAnnotationFiltering() throws Exception {
        Path outputPath = getDefaultPath();
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);
        try (GraphContext context = factory.create(outputPath, true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/annotationtests/basic";

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    BasicAnnotationScanProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            Assert.assertEquals(1, basicProvider.baseRuleHitCount);
            Assert.assertEquals(1, basicProvider.withValueFilterHitCount);
            Assert.assertEquals(0, basicProvider.withIncorrectFilterCount);
        }
    }

    @Test
    public void testComplexAnnotationFiltering() throws Exception {
        Path outputPath = getDefaultPath();
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/annotationtests/complex";

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    ComplexAnnotationScanProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            Assert.assertEquals(1, complexProvider.baseRuleHitCount);
            Assert.assertEquals(1, complexProvider.nestedAnnotationHitCount);
            Assert.assertEquals(0, complexProvider.nestedAnnotationWrongNameHitCount);
            Assert.assertEquals(1, complexProvider.nestedAnnotationWithNullLiteralShouldMatch);
            Assert.assertEquals(0, complexProvider.nestedAnnotationWithNullLiteralShouldNotMatchNull);
        }
    }

    @Test
    public void testRegexAnnotationFiltering() throws Exception {
        Path outputPath = getDefaultPath();
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/annotationtests/regex";

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(RegexAnnotationScanProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            Assert.assertEquals(3, regexProvider.baseRuleHitCount);
            Assert.assertEquals(1, regexProvider.withValueFilterHitCount);
            Assert.assertEquals(0, regexProvider.withIncorrectFilterCount);
            Assert.assertEquals(1, regexProvider.baseValueRuleHitCount);
            Assert.assertEquals(1, regexProvider.withRegexFilterHitCount);
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_javaclassannotationfilteringtest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class BasicAnnotationScanProvider extends AbstractRuleProvider {
        private int baseRuleHitCount = 0;
        private int withValueFilterHitCount = 0;
        private int withIncorrectFilterCount = 0;

        public BasicAnnotationScanProvider() {
            super(MetadataBuilder.forProvider(BasicAnnotationScanProvider.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.basic.SimpleTestAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION)
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            baseRuleHitCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.basic.SimpleTestAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION).annotationMatches("value2", new AnnotationLiteralCondition("value 2"))
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            withValueFilterHitCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.basic.SimpleTestAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION).annotationMatches("value2", new AnnotationLiteralCondition("wrongvalue"))
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            withIncorrectFilterCount++;
                        }
                    });
        }
        // @formatter:on
    }

    @Singleton
    public static class ComplexAnnotationScanProvider extends AbstractRuleProvider {
        private int baseRuleHitCount = 0;
        private int nestedAnnotationHitCount = 0;
        private int nestedAnnotationWrongNameHitCount = 0;
        private int nestedAnnotationWithNullLiteralShouldMatch = 0;
        private int nestedAnnotationWithNullLiteralShouldNotMatchNull = 0;

        public ComplexAnnotationScanProvider() {
            super(MetadataBuilder.forProvider(ComplexAnnotationScanProvider.class).setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.complex.ExampleComplexAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION)
                                    .annotationMatches(
                                            "nestedAnnotationArray",
                                            new AnnotationListCondition(0).addCondition(new AnnotationTypeCondition("org.jboss.windup.rules.annotationtests.complex.ExampleNestedAnnotation"))
                                    )
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            baseRuleHitCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("javax.annotation.sql.DataSourceDefinitions")
                                    .at(TypeReferenceLocation.ANNOTATION)
                                    .annotationMatches(
                                            "value",
                                            new AnnotationListCondition(0).addCondition(new AnnotationTypeCondition("javax.annotation.sql.DataSourceDefinition"))
                                    )
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            nestedAnnotationHitCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("javax.annotation.sql.DataSourceDefinitions")
                                    .at(TypeReferenceLocation.ANNOTATION)
                                    .annotationMatches(
                                            "wrongValue",
                                            new AnnotationListCondition(0).addCondition(new AnnotationTypeCondition("javax.annotation.sql.DataSourceDefinition"))
                                    )
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            nestedAnnotationWrongNameHitCount++;
                        }
                    })

                    .addRule().when(
                            JavaClass.references("javax.annotation.sql.DataSourceDefinitions")
                                    .at(TypeReferenceLocation.ANNOTATION)
                                    .annotationMatches(
                                            "value",
                                            new AnnotationListCondition(2).addCondition(
                                                    new AnnotationTypeCondition("javax.annotation.sql.DataSourceDefinition")
                                                            .addCondition("serverName", new AnnotationLiteralCondition(null))
                                            )
                                    )
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            nestedAnnotationWithNullLiteralShouldMatch++;
                        }
                    })

                    .addRule().when(
                            JavaClass.references("javax.annotation.sql.DataSourceDefinitions")
                                    .at(TypeReferenceLocation.ANNOTATION)
                                    .annotationMatches(
                                            "value",
                                            new AnnotationListCondition(2).addCondition(
                                                    new AnnotationTypeCondition("javax.annotation.sql.DataSourceDefinition")
                                                            .addCondition("className", new AnnotationLiteralCondition("com.example.HasSOmeNulls"))
                                                            .addCondition("serverName", new AnnotationLiteralCondition("{*}"))
                                            )
                                    )
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            nestedAnnotationWithNullLiteralShouldNotMatchNull++;
                        }
                    });
        }
        // @formatter:on
    }

    @Singleton
    public static class RegexAnnotationScanProvider extends AbstractRuleProvider {
        private int baseRuleHitCount = 0;
        private int withValueFilterHitCount = 0;
        private int withIncorrectFilterCount = 0;
        private int baseValueRuleHitCount = 0;
        private int withRegexFilterHitCount = 0;

        public RegexAnnotationScanProvider() {
            super(MetadataBuilder.forProvider(RegexAnnotationScanProvider.class).setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.regex.SimpleTestAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION)
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            baseRuleHitCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.regex.SimpleTestAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION).annotationMatches("value2", new AnnotationLiteralCondition("value {accepted_value}"))
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            withValueFilterHitCount++;
                        }
                    }).where("accepted_value").matches("4")
                    .addRule().when(
                            JavaClass.references("org.jboss.windup.rules.annotationtests.regex.SimpleTestAnnotation")
                                    .at(TypeReferenceLocation.ANNOTATION).annotationMatches("value2", new AnnotationLiteralCondition("wrongvalue"))
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            withIncorrectFilterCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("java.lang.String")
                                    .at(TypeReferenceLocation.FIELD_DECLARATION).annotationMatches(new AnnotationTypeCondition("org.jboss.windup.rules.annotationtests.regex.SimpleTestAnnotation").addCondition("value2", new AnnotationLiteralCondition("member value 2")))
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            baseValueRuleHitCount++;
                        }
                    })
                    .addRule().when(
                            JavaClass.references("java.lang.String")
                                    .at(TypeReferenceLocation.FIELD_DECLARATION).annotationMatches(new AnnotationTypeCondition("{annotation_type}").addCondition("value2", new AnnotationLiteralCondition("{annotation_value_2}")))
                    ).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            withRegexFilterHitCount++;
                        }
                    }).where("annotation_type").matches("org.jboss.windup.rules.annotationtests.regex.SimpleTestAnnotation")
                    .where("annotation_value_2").matches(".*2");

        }
        // @formatter:on
    }
}
