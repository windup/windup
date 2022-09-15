package org.jboss.windup.tooling;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.reporting.model.ReplacementQuickfixModel;
import org.jboss.windup.reporting.model.TransformationQuickfixModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.tooling.data.Classification;
import org.jboss.windup.tooling.data.ClassificationImpl;
import org.jboss.windup.tooling.data.Hint;
import org.jboss.windup.tooling.data.HintImpl;
import org.jboss.windup.tooling.data.IssueCategoryImpl;
import org.jboss.windup.tooling.data.Link;
import org.jboss.windup.tooling.data.LinkImpl;
import org.jboss.windup.tooling.data.Quickfix;
import org.jboss.windup.tooling.data.QuickfixImpl;
import org.jboss.windup.tooling.data.ReportLink;
import org.jboss.windup.tooling.data.ReportLinkImpl;
import org.jboss.windup.util.exception.WindupException;

/**
 * Contains an implementation of {@link ExecutionResults} that loads its results from a {@link GraphContext}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@XmlRootElement(name = "execution-results")
public class ExecutionResultsImpl implements ExecutionResults {
    private static final long serialVersionUID = 1L;

    private final ToolingXMLService toolingXMLService;

    private final List<Classification> classifications;
    private final List<Hint> hints;
    private final List<ReportLink> reportLinks;
    private String windupStopOnRequestMessage;

    public ExecutionResultsImpl() {
        this.toolingXMLService = null;
        this.windupStopOnRequestMessage = null;
        this.classifications = Collections.emptyList();
        this.hints = Collections.emptyList();
        this.reportLinks = Collections.emptyList();
    }

    public ExecutionResultsImpl(GraphContext graphContext, ToolingXMLService toolingXMLService) {
        this.toolingXMLService = toolingXMLService;
        this.classifications = getClassifications(graphContext);
        this.hints = getHints(graphContext);
        this.reportLinks = getReportLinks(graphContext);
    }

    private static List<ReportLink> getReportLinks(GraphContext graphContext) {
        final List<ReportLink> reportLinks = new ArrayList<>();
        SourceReportService sourceReportService = new SourceReportService(graphContext);
        ReportService reportService = new ReportService(graphContext);
        for (SourceReportModel sourceReportModel : sourceReportService.findAll()) {
            ReportLinkImpl reportLink = new ReportLinkImpl();
            reportLink.setInputFile(sourceReportModel.getSourceFileModel().asFile());
            Path reportPath = reportService.getReportDirectory().resolve(sourceReportModel.getReportFilename());
            reportLink.setReportFile(reportPath.toFile());
            reportLinks.add(reportLink);
        }
        return reportLinks;
    }

    private static List<Hint> getHints(GraphContext graphContext) {
        final List<Hint> hints = new ArrayList<>();
        InlineHintService hintService = new InlineHintService(graphContext);
        for (InlineHintModel hintModel : hintService.findAll()) {
            HintImpl hint = new HintImpl(hintModel.getElement().id());
            hint.setFile(hintModel.getFile().asFile());
            hint.setTitle(hintModel.getTitle());
            hint.setHint(hintModel.getHint());
            hint.setIssueCategory(new IssueCategoryImpl(hintModel.getIssueCategory()));
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

    private static List<Classification> getClassifications(GraphContext graphContext) {
        final List<Classification> classifications = new ArrayList<>();
        ClassificationService classificationService = new ClassificationService(graphContext);
        for (ClassificationModel classificationModel : classificationService.findAll()) {
            for (FileModel fileModel : classificationModel.getFileModels()) {
                ClassificationImpl classification = new ClassificationImpl(classificationModel.getElement().id());
                classification.setClassification(classificationModel.getClassification());
                classification.setDescription(classificationModel.getDescription());
                classification.setEffort(classificationModel.getEffort());
                classification.setRuleID(classificationModel.getRuleID());
                classification.setIssueCategory(new IssueCategoryImpl(classificationModel.getIssueCategory()));
                classification.setFile(fileModel.asFile());

                classification.setLinks(asLinks(classificationModel.getLinks()));
                classifications.add(classification);

                classification.setQuickfixes(asQuickfixes(classificationModel.getQuickfixes()));
            }
        }
        return classifications;
    }

    private static List<Link> asLinks(Iterable<LinkModel> linkModels) {
        List<Link> links = new ArrayList<>();
        for (LinkModel linkModel : linkModels) {
            LinkImpl link = new LinkImpl();
            link.setDescription(linkModel.getDescription());
            link.setUrl(linkModel.getLink());
            links.add(link);
        }
        return links;
    }

    private static List<Quickfix> asQuickfixes(Iterable<QuickfixModel> quickfixModels) {
        List<Quickfix> fixes = new ArrayList<>();
        for (QuickfixModel quickfixModel : quickfixModels) {
            QuickfixImpl quickfix = new QuickfixImpl();
            quickfix.setType(org.jboss.windup.tooling.data.QuickfixType.valueOf(quickfixModel.getQuickfixType().name()));
            quickfix.setName(quickfixModel.getName());

            if (quickfixModel instanceof ReplacementQuickfixModel) {
                ReplacementQuickfixModel replacementQuickfixModel = (ReplacementQuickfixModel) quickfixModel;
                quickfix.setNewline(replacementQuickfixModel.getNewline());
                quickfix.setReplacement(replacementQuickfixModel.getReplacement());
                quickfix.setSearch(replacementQuickfixModel.getSearch());
            }

            if (quickfixModel instanceof TransformationQuickfixModel) {
                TransformationQuickfixModel transformationQuickfixModel = (TransformationQuickfixModel) quickfixModel;
                quickfix.setTransformationID(transformationQuickfixModel.getTransformationID());
                FileModel fileModel = transformationQuickfixModel.getFile();
                if (fileModel != null) {
                    quickfix.setFile(fileModel.asFile());
                }
            }

            fixes.add(quickfix);
        }
        return fixes;
    }

    @Override
    @XmlAttribute(name = "stopMessage")
    public String getWindupStopOnRequestMessage() {
        return this.windupStopOnRequestMessage;
    }

    @Override
    @XmlElementWrapper(name = "classifications")
    @XmlElement(name = "classification", type = ClassificationImpl.class)
    public List<Classification> getClassifications() {
        return classifications;
    }

    @Override
    @XmlElementWrapper(name = "hints")
    @XmlElement(name = "hint", type = HintImpl.class)
    public List<Hint> getHints() {
        return hints;
    }

    @Override
    @XmlElementWrapper(name = "report-links")
    @XmlElement(name = "report-link", type = ReportLinkImpl.class)
    public List<ReportLink> getReportLinks() {
        return reportLinks;
    }

    @Override
    public void serializeToXML(Path path) {
        try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
            toolingXMLService.serializeResults(this, outputStream);
        } catch (IOException e) {
            throw new WindupException("Failed to serialize results due to I/O Error: " + e.getMessage(), e);
        }
    }
}
