package org.jboss.windup.rules.apps.java.service;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;

public class TypeReferenceService extends GraphService<TypeReferenceModel>
{
    @Inject
    public TypeReferenceService(GraphContext context)
    {
        super(context, TypeReferenceModel.class);
    }

    public TypeReferenceModel createTypeReference(FileModel fileModel, TypeReferenceLocation location,
                int lineNumber, int columnNumber, int length, String source)
    {
        TypeReferenceModel model = create();

        model.setFile(fileModel);
        model.setLineNumber(lineNumber);
        model.setColumnNumber(columnNumber);
        model.setLength(length);
        model.setSourceSnippit(source);
        model.setReferenceLocation(location);

        return model;
    }

}
