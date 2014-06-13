package org.jboss.windup.rules.apps.javascanner.ast.event;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.javascanner.ast.ClassCandidate;

public class JavaScannerASTEvent
{
    private final GraphContext context;
    private final FileModel fileModel;
    private final ClassCandidate classCandidate;

    public JavaScannerASTEvent(GraphContext context, FileModel fileModel, ClassCandidate classCandidate)
    {
        super();
        this.context = context;
        this.fileModel = fileModel;
        this.classCandidate = classCandidate;
    }

    public GraphContext getContext()
    {
        return context;
    }

    public FileModel getFileModel()
    {
        return fileModel;
    }

    public ClassCandidate getClassCandidate()
    {
        return classCandidate;
    }

    @Override
    public String toString()
    {
        return "JavaScannerASTEvent [context=" + context + ", fileModel=" + fileModel + ", classCandidate="
                    + classCandidate + "]";
    }
}
