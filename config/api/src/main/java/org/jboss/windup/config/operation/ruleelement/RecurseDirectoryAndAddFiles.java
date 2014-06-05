package org.jboss.windup.config.operation.ruleelement;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class RecurseDirectoryAndAddFiles extends AbstractIterationOperator<FileResourceModel>
{
    public RecurseDirectoryAndAddFiles(String variableName)
    {
        super(FileResourceModel.class, variableName);
    }

    public static RecurseDirectoryAndAddFiles add(String variableName)
    {
        return new RecurseDirectoryAndAddFiles(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileResourceModel resourceModel)
    {
        visitFile(event.getGraphContext(), resourceModel);
    }

    private void visitFile(GraphContext ctx, FileResourceModel file)
    {
        String filePath = file.getFilePath();
        File fileReference = new File(filePath);

        if (fileReference.isDirectory())
        {
            Collection<File> found = FileUtils.listFiles(fileReference, FileFileFilter.FILE, TrueFileFilter.INSTANCE);
            for (File reference : found)
            {
                FileResourceModel subFile = ctx.getFramed().addVertex(null, FileResourceModel.class);
                subFile.setFilePath(reference.getAbsolutePath());
                visitFile(ctx, subFile);
            }
        }

    }
}
