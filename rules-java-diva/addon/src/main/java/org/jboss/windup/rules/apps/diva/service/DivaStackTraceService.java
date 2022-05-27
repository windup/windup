package org.jboss.windup.rules.apps.diva.service;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.FileLocationService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.rules.apps.diva.model.DivaStackTraceModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

public class DivaStackTraceService extends GraphService<DivaStackTraceModel> {

    FileLocationService fileLocationService;

    public DivaStackTraceService(GraphContext context) {
        super(context, DivaStackTraceModel.class);
        fileLocationService = new FileLocationService(context);
    }

    public DivaStackTraceModel getOrCreate(FileModel fileModel, int lineNumber, int columnNumber, int length,
            DivaStackTraceModel parent, JavaMethodModel method) {

        List<? extends FileLocationModel> locs = fileModel
                .traverse(g -> g.in(FileReferenceModel.FILE_MODEL).has(WindupFrame.TYPE_PROP, FileLocationModel.TYPE)
                        .has(FileLocationModel.COLUMN_NUMBER, columnNumber)
                        .has(FileLocationModel.LINE_NUMBER, lineNumber).has(FileLocationModel.LENGTH, length))
                .toList(FileLocationModel.class);

        FileLocationModel location;
        DivaStackTraceModel model = null;
        if (locs.isEmpty()) {
            location = fileLocationService.create();
            location.setColumnNumber(columnNumber);
            location.setLineNumber(lineNumber);
            location.setLength(length);
            location.setFile(fileModel);
            if (fileModel instanceof SourceFileModel) {
                ((SourceFileModel) fileModel).setGenerateSourceReport(true);
                InlineHintModel inlineHint = location instanceof InlineHintModel ? (InlineHintModel) location
                        : addTypeToModel(getGraphContext(), location, InlineHintModel.class);
                if (inlineHint.getTitle() == null) {
                    inlineHint.setTitle("Transactions report");
                    inlineHint.setIssueDisplayMode(IssueDisplayMode.DETAIL_ONLY);
                    inlineHint.setHint("---");
                    inlineHint.setEffort(0);
                } else {
                    inlineHint.setTitle(inlineHint.getTitle() + ", Transactions report");
                }
            }
        } else {
            location = locs.get(0);
            GraphTraversal<?, ?> traversal = new GraphTraversalSource(getGraphContext().getGraph())
                    .V(location.getElement()).in(DivaStackTraceModel.LOCATION);
            if (parent == null) {
                traversal = traversal.not(__.out(DivaStackTraceModel.PARENT));
            } else {
                traversal = traversal.filter(__.out(DivaStackTraceModel.PARENT).is(parent.getElement()));
            }
            model = getUnique(traversal);
        }

        if (model == null) {
            model = create();
            model.setLocation(location);

            if (parent != null) {
                model.setParent(parent);
            }
            if (method != null) {
                model.setMethod(method);
            }
        }
        return model;
    }

}
