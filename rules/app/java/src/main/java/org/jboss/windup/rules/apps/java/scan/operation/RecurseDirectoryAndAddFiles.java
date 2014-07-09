package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class RecurseDirectoryAndAddFiles extends AbstractIterationOperation<FileModel>
{
    private RecurseDirectoryAndAddFiles(String variableName)
    {
        super(FileModel.class, variableName);
    }

    public static RecurseDirectoryAndAddFiles startingAt(String variableName)
    {
        return new RecurseDirectoryAndAddFiles(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel resourceModel)
    {
        visitFile(event.getGraphContext(), resourceModel);
    }

    private void visitFile(GraphContext ctx, FileModel file)
    {
        String filePath = file.getFilePath();
        File fileReference = new File(filePath);

        if (fileReference.isDirectory())
        {
            Collection<File> found = FileUtils.listFiles(fileReference, FileFileFilter.FILE, TrueFileFilter.INSTANCE);
            for (File reference : found)
            {
                FileModel subFile = ctx.getFramed().addVertex(null, FileModel.class);
                subFile.setFilePath(reference.getAbsolutePath());
                visitFile(ctx, subFile);
            }
        }

    }
}
