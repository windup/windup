package org.jboss.windup.addon.config.operation.ruleelement;

import java.io.File;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.engine.decompilers.api.DecompilationException;
import org.jboss.windup.engine.decompilers.api.Decompiler;
import org.jboss.windup.engine.decompilers.procyon.ProcyonConf;
import org.jboss.windup.engine.decompilers.procyon.ProcyonDecompiler;
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
            Decompiler.Jar decompiler = new ProcyonDecompiler();
            String jarFilePath = ((FileResourceModel) payload).getFilePath();
            File jarFile = new File(jarFilePath);
            File destinationPath = new File(payload.getUnzippedDirectory().getFilePath());

            ProcyonConf conf = new ProcyonConf();
            conf.setIncludeNested(false);
            try
            {
                decompiler.decompileJar(jarFile, destinationPath, conf);
            }
            catch (DecompilationException exc)
            {
                throw new WindupException("Error decompiling archive " + jarFilePath + " due to: " + exc.getMessage(),
                            exc);
            }
        }
    }
}