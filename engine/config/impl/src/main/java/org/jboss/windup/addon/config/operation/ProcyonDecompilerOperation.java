package org.jboss.windup.addon.config.operation;

import java.io.File;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.engine.decompiler.DecompilationException;
import org.jboss.windup.engine.decompiler.Decompiler;
import org.jboss.windup.engine.decompiler.procyon.ProcyonConfiguration;
import org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler;
import org.jboss.windup.engine.util.exception.WindupException;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ProcyonDecompilerOperation extends AbstractIterationOperator<ArchiveModel>
{

    public ProcyonDecompilerOperation(String variableName)
    {
        super(ArchiveModel.class, variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        if (payload.getUnzippedDirectory() != null)
        {
            Decompiler decompiler = new ProcyonDecompiler(new ProcyonConfiguration().setIncludeNested(false));
            String archivePath = ((FileResourceModel) payload).getFilePath();
            File archive = new File(archivePath);
            File outputDir = new File(payload.getUnzippedDirectory().getFilePath());

            try
            {
                decompiler.decompileArchive(archive, outputDir);
            }
            catch (final DecompilationException exc)
            {
                throw new WindupException("Error decompiling archive " + archivePath + " due to: " + exc.getMessage(),
                            exc);
            }
        }
    }
}