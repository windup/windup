package org.jboss.windup.tooling;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Lists;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.config.ExcludePackagesOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.tooling.data.Classification;
import org.jboss.windup.tooling.data.Hint;
import org.jboss.windup.tooling.data.Link;
import org.jboss.windup.tooling.data.ReportLink;
import org.jboss.windup.tooling.org.jboss.windup.tooling.data.ClassificationImpl;
import org.jboss.windup.tooling.org.jboss.windup.tooling.data.HintImpl;
import org.jboss.windup.tooling.org.jboss.windup.tooling.data.LinkImpl;
import org.jboss.windup.tooling.org.jboss.windup.tooling.data.ReportLinkImpl;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ExecutionBuilderImpl implements ExecutionBuilder, ExecutionBuilderSetInput, ExecutionBuilderSetOutput, ExecutionBuilderSetOptions,
            ExecutionBuilderSetOptionsAndProgressMonitor
{
    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private WindupProcessor processor;

    private Path windupHome;
    private WindupProgressMonitor progressMonitor;
    private Path input;
    private Path output;
    private Set<String> ignorePathPatterns = new HashSet<>();
    private Set<String> includePackagePrefixSet = new HashSet<>();
    private Set<String> excludePackagePrefixSet = new HashSet<>();

    @Override
    public ExecutionBuilderSetInput begin(Path windupHome)
    {
        this.windupHome = windupHome;
        return this;
    }

    @Override
    public ExecutionBuilderSetOutput setInput(Path input)
    {
        this.input = input;
        return this;
    }

    @Override
    public ExecutionBuilderSetOptionsAndProgressMonitor setOutput(Path output)
    {
        this.output = output;
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions ignore(String ignorePattern)
    {
        this.ignorePathPatterns.add(ignorePattern);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions includePackage(String packagePrefix)
    {
        this.includePackagePrefixSet.add(packagePrefix);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions excludePackage(String packagePrefix)
    {
        this.excludePackagePrefixSet.add(packagePrefix);
        return this;
    }

    @Override
    public ExecutionBuilderSetOptions setProgressMonitor(WindupProgressMonitor monitor)
    {
        this.progressMonitor = monitor;
        return this;
    }

    @Override
    public ExecutionResults execute()
    {
        PathUtil.setWindupHome(this.windupHome);
        WindupConfiguration windupConfiguration = new WindupConfiguration();
        try
        {
            windupConfiguration.useDefaultDirectories();
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to configure windup due to: " + e.getMessage(), e);
        }
        windupConfiguration.setInputPath(this.input);
        windupConfiguration.setOutputDirectory(this.output);
        windupConfiguration.setProgressMonitor(this.progressMonitor);

        Path graphPath = output.resolve("graph");
        try (final GraphContext graphContext = graphContextFactory.create(graphPath))
        {

            GraphService<IgnoredFileRegexModel> graphService = new GraphService<>(graphContext, IgnoredFileRegexModel.class);
            WindupJavaConfigurationService windupJavaConfigurationService = new WindupJavaConfigurationService(graphContext);
            for (String ignorePattern : this.ignorePathPatterns)
            {
                IgnoredFileRegexModel ignored = graphService.create();
                ignored.setRegex(ignorePattern);

                WindupJavaConfigurationModel javaCfg = windupJavaConfigurationService.getJavaConfigurationModel(graphContext);
                javaCfg.addIgnoredFileRegex(ignored);
            }

            windupConfiguration.setOptionValue(ScanPackagesOption.NAME, Lists.toList(this.includePackagePrefixSet));
            windupConfiguration.setOptionValue(ExcludePackagesOption.NAME, Lists.toList(this.excludePackagePrefixSet));

            windupConfiguration
                        .setProgressMonitor(progressMonitor)
                        .setGraphContext(graphContext);
            processor.execute(windupConfiguration);

            final List<Classification> classifications = getClassifications(graphContext);
            final List<Hint> hints = getHints(graphContext);
            final List<ReportLink> reportLinks = getReportLinks(graphContext);

            return new ExecutionResults()
            {
                @Override
                public Iterable<Classification> getClassifications()
                {
                    return classifications;
                }

                @Override
                public Iterable<Hint> getHints()
                {
                    return hints;
                }

                @Override
                public Iterable<ReportLink> getReportLinks()
                {
                    return reportLinks;
                }
            };
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to instantiate graph due to: " + e.getMessage(), e);
        }
    }

    private List<ReportLink> getReportLinks(GraphContext graphContext)
    {
        final List<ReportLink> reportLinks = new ArrayList<>();
        SourceReportService sourceReportService = new SourceReportService(graphContext);
        ReportService reportService = new ReportService(graphContext);
        for (SourceReportModel sourceReportModel : sourceReportService.findAll())
        {
            ReportLinkImpl reportLink = new ReportLinkImpl();
            reportLink.setInputFile(sourceReportModel.getSourceFileModel().asFile());
            Path reportPath = Paths.get(reportService.getReportDirectory()).resolve(sourceReportModel.getReportFilename());
            reportLink.setReportFile(reportPath.toFile());
            reportLinks.add(reportLink);
        }
        return reportLinks;
    }

    private List<Hint> getHints(GraphContext graphContext)
    {
        final List<Hint> hints = new ArrayList<>();
        InlineHintService hintService = new InlineHintService(graphContext);
        for (InlineHintModel hintModel : hintService.findAll())
        {
            HintImpl hint = new HintImpl();
            hint.setFile(hintModel.getFile().asFile());
            hint.setTitle(hintModel.getTitle());
            hint.setHint(hintModel.getHint());
            hint.setSeverity(hintModel.getSeverity());
            hint.setEffort(hintModel.getEffort());
            hint.setColumn(hintModel.getColumnNumber());
            hint.setLineNumber(hintModel.getLineNumber());
            hint.setLength(hintModel.getLength());
            hint.setSourceSnippit(hintModel.getSourceSnippit());
            hint.setRuleID(hintModel.getRuleID());

            hint.setLinks(asLinks(hintModel.getLinks()));
            hints.add(hint);
        }
        return hints;
    }

    private List<Classification> getClassifications(GraphContext graphContext)
    {
        final List<Classification> classifications = new ArrayList<>();
        ClassificationService classificationService = new ClassificationService(graphContext);
        for (ClassificationModel classificationModel : classificationService.findAll())
        {
            for (FileModel fileModel : classificationModel.getFileModels())
            {
                ClassificationImpl classification = new ClassificationImpl();
                classification.setClassification(classificationModel.getClassification());
                classification.setDescription(classificationModel.getDescription());
                classification.setEffort(classificationModel.getEffort());
                classification.setRuleID(classificationModel.getRuleID());
                classification.setSeverity(classificationModel.getSeverity());
                classification.setFile(fileModel.asFile());

                classification.setLinks(asLinks(classificationModel.getLinks()));
                classifications.add(classification);
            }
        }
        return classifications;
    }

    private List<Link> asLinks(Iterable<LinkModel> linkModels)
    {
        List<Link> links = new ArrayList<>();
        for (LinkModel linkModel : linkModels)
        {
            LinkImpl link = new LinkImpl();
            link.setDescription(linkModel.getDescription());
            link.setUrl(linkModel.getLink());
            links.add(link);
        }
        return links;
    }
}
