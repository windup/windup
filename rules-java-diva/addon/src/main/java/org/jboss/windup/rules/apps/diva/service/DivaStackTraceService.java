package org.jboss.windup.rules.apps.diva.service;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileLocationService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.diva.model.DivaStackTraceModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

public class DivaStackTraceService extends GraphService<DivaStackTraceModel> {

    FileLocationService fileLocationService;

    public DivaStackTraceService(GraphContext context) {
        super(context, DivaStackTraceModel.class);
        fileLocationService = new FileLocationService(context);
    }

//    static int count = 0;
//    static long total = 0;

    public DivaStackTraceModel getOrCreate(FileModel fileModel, int lineNumber, int columnNumber, int length,
            DivaStackTraceModel parent, JavaMethodModel method) {
        // FileModel fileModel = fileService.createByFilePath(filePath);
        GraphTraversal<?, ?> traversal = getGraphContext().getQuery(FileLocationModel.class).getRawTraversal()
                .has(FileLocationModel.COLUMN_NUMBER, columnNumber).has(FileLocationModel.LINE_NUMBER, lineNumber)
                .has(FileLocationModel.LENGTH, length)
                .filter(__.out(FileReferenceModel.FILE_MODEL).is(fileModel.getElement()));

        List<?> locs = traversal.toList();

        FileLocationModel location;
        DivaStackTraceModel model = null;
        if (locs.isEmpty()) {
            location = fileLocationService.create();
            location.setColumnNumber(columnNumber);
            location.setLineNumber(lineNumber);
            location.setLength(length);
            location.setFile(fileModel);

        } else {
            location = (FileLocationModel) locs.get(0);
            traversal = getQuery().getRawTraversal().has(DivaStackTraceModel.LOCATION, location.getElement());
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
