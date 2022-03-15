package org.jboss.windup.rules.apps.diva.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.diva.model.DivaStackTraceModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

public class DivaStackTraceService extends GraphService<DivaStackTraceModel> {

    FileService fileService;

    public DivaStackTraceService(GraphContext context) {
        super(context, DivaStackTraceModel.class);
        fileService = new FileService(context);
    }

    public void setFilePath(DivaStackTraceModel model, String filePath) {
        model.setFile(fileService.createByFilePath(filePath));
    }

//    static int count = 0;
//    static long total = 0;

    public DivaStackTraceModel getOrCreate(FileModel fileModel, int lineNumber, int columnNumber, int length,
            DivaStackTraceModel parent, JavaMethodModel method) {
        // FileModel fileModel = fileService.createByFilePath(filePath);
        GraphTraversal<?, ?> traversal = getQuery().getRawTraversal().has(FileLocationModel.COLUMN_NUMBER, columnNumber)
                .has(FileLocationModel.LINE_NUMBER, lineNumber).has(FileLocationModel.LENGTH, length)
                .filter(__.out(FileReferenceModel.FILE_MODEL).is(fileModel.getElement()));
        if (parent == null) {
            traversal = traversal.not(__.out(DivaStackTraceModel.PARENT));
        } else {
            traversal = traversal.filter(__.out(DivaStackTraceModel.PARENT).is(parent.getElement()));
        }
       DivaStackTraceModel model = getUnique(traversal);
//        }
        if (model == null) {
            model = create();
            model.setColumnNumber(columnNumber);
            model.setLineNumber(lineNumber);
            model.setLength(length);
            model.setFile(fileModel);
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
