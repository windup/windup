package org.jboss.windup.rules.apps.diva.service;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
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

    // public static int count0 = 0;
    // public static long total0 = 0;
    // public static int count1 = 0;
    // public static long total1 = 0;
    // public static int count2 = 0;
    // public static long total2 = 0;

    public DivaStackTraceModel getOrCreate(FileModel fileModel, int lineNumber, int columnNumber, int length,
            DivaStackTraceModel parent, JavaMethodModel method) {
        // FileModel fileModel = fileService.createByFilePath(filePath);
        GraphTraversal<?, ?> traversal = getGraphContext().getQuery(FileLocationModel.class).getRawTraversal()
                .has(FileLocationModel.COLUMN_NUMBER, columnNumber).has(FileLocationModel.LINE_NUMBER, lineNumber)
                .has(FileLocationModel.LENGTH, length)
                .filter(__.out(FileReferenceModel.FILE_MODEL).is(fileModel.getElement()));

        List<?> locs = traversal.toList();

        // if (count0++ % 100 == 0) {
        // System.out.println(count0 + ", " + (total0 / 1000000D) + "ms, " + count1 + ",
        // " + (total1 / 1000000D)
        // + "ms, " + count2 + ", " + (total2 / 1000000D) + "ms");
        // }

        FileLocationModel location;
        DivaStackTraceModel model = null;
        if (locs.isEmpty()) {
            location = fileLocationService.create();
            location.setColumnNumber(columnNumber);
            location.setLineNumber(lineNumber);
            location.setLength(length);
            location.setFile(fileModel);
            if (fileModel instanceof SourceFileModel) {
                ((SourceFileModel)fileModel).setGenerateSourceReport(true);
                InlineHintModel inlineHint = addTypeToModel(getGraphContext(), location, InlineHintModel.class);
                inlineHint.setTitle("line = " + lineNumber + ", col = " + columnNumber);
                inlineHint.setIssueDisplayMode(IssueDisplayMode.DETAIL_ONLY);
            }
        } else {
            location = fileLocationService.frame((Vertex) locs.get(0));
            traversal = getQuery().getRawTraversal()
                    .filter(__.out(DivaStackTraceModel.LOCATION).is(location.getElement()));
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
