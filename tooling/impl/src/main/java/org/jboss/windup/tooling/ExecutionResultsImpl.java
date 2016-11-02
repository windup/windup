package org.jboss.windup.tooling;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.reporting.severity.IssueCategory;
import org.jboss.windup.tooling.data.Classification;
import org.jboss.windup.tooling.data.ClassificationImpl;
import org.jboss.windup.tooling.data.Hint;
import org.jboss.windup.tooling.data.HintImpl;
import org.jboss.windup.tooling.data.Link;
import org.jboss.windup.tooling.data.LinkImpl;
import org.jboss.windup.tooling.data.Quickfix;
import org.jboss.windup.tooling.data.QuickfixImpl;
import org.jboss.windup.tooling.data.ReportLink;
import org.jboss.windup.tooling.data.ReportLinkImpl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains an implementation of {@link ExecutionResults} that loads its results from a {@link GraphContext}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public class ExecutionResultsImpl implements ExecutionResults
{
    private final List<Classification> classifications;
    private final List<Hint> hints;
    private final List<ReportLink> reportLinks;

    public ExecutionResultsImpl(GraphContext graphContext)
    {
        this.classifications = getClassifications(graphContext);
        this.hints = getHints(graphContext);
        this.reportLinks = getReportLinks(graphContext);
    }

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

    private List<ReportLink> getReportLinks(GraphContext graphContext)
    {
        final List<ReportLink> reportLinks = new ArrayList<>();
        SourceReportService sourceReportService = new SourceReportService(graphContext);
        ReportService reportService = new ReportService(graphContext);
        for (SourceReportModel sourceReportModel : sourceReportService.findAll())
        {
            ReportLinkImpl reportLink = new ReportLinkImpl();
            reportLink.setInputFile(sourceReportModel.getSourceFileModel().asFile());
            Path reportPath = reportService.getReportDirectory().resolve(sourceReportModel.getReportFilename());
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
            HintImpl hint = new HintImpl(hintModel.asVertex().getId());
            hint.setFile(hintModel.getFile().asFile());
            hint.setTitle(hintModel.getTitle());
            hint.setHint(hintModel.getHint());
            hint.setIssueCategory(new IssueCategory(hintModel.getIssueCategory()));
            hint.setEffort(hintModel.getEffort());
            hint.setColumn(hintModel.getColumnNumber());
            hint.setLineNumber(hintModel.getLineNumber());
            hint.setLength(hintModel.getLength());
            hint.setSourceSnippit(hintModel.getSourceSnippit());
            hint.setRuleID(hintModel.getRuleID());
            hint.setQuickfixes(asQuickfixes(hintModel.getQuickfixes()));

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
                ClassificationImpl classification = new ClassificationImpl(classificationModel.asVertex().getId());
                classification.setClassification(classificationModel.getClassification());
                classification.setDescription(classificationModel.getDescription());
                classification.setEffort(classificationModel.getEffort());
                classification.setRuleID(classificationModel.getRuleID());
                classification.setIssueCategory(new IssueCategory(classificationModel.getIssueCategory()));
                classification.setFile(fileModel.asFile());

                classification.setLinks(asLinks(classificationModel.getLinks()));
                classifications.add(classification);
                
                classification.setQuickfixes(asQuickfixes(classificationModel.getQuickfixes()));
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
    
    private List<Quickfix> asQuickfixes(Iterable<QuickfixModel> quickfixModels)
    {
        List<Quickfix> fixes = new ArrayList<>();
        for (QuickfixModel quickfixModel : quickfixModels)
        {
            QuickfixImpl quickfix = new QuickfixImpl();
            quickfix.setType(quickfixModel.getQuickfixType());
            quickfix.setName(quickfixModel.getName());
            quickfix.setNewline(quickfixModel.getNewline());
            quickfix.setReplacement(quickfixModel.getReplacement());
            quickfix.setSearch(quickfixModel.getSearch());

            fixes.add(quickfix);
        }
        return fixes;
    }
}
