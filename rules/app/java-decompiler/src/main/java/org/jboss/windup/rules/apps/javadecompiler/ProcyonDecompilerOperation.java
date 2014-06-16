package org.jboss.windup.rules.apps.javadecompiler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.procyon.ProcyonConfiguration;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ProcyonDecompilerOperation extends AbstractIterationOperator<ArchiveModel>
{

    public ProcyonDecompilerOperation(String variableName)
    {
        super(ArchiveModel.class, variableName);
    }

    @Override
    public void perform(final GraphRewrite event, final EvaluationContext context, final ArchiveModel payload)
    {
        if (payload.getUnzippedDirectory() != null)
        {
            Decompiler decompiler = new ProcyonDecompiler(new ProcyonConfiguration().setIncludeNested(false));
            String archivePath = ((FileModel) payload).getFilePath();
            File archive = new File(archivePath);
            File outputDir = new File(payload.getUnzippedDirectory().getFilePath());

            try
            {
                DecompilationResult result = decompiler.decompileArchive(archive, outputDir);
                Set<String> decompiledOutputFileSet = result.getDecompiledOutputFiles();

                GraphService<FileModel> fileService = new GraphService<>(event.getGraphContext(),
                            FileModel.class);
                for (String decompiledOutputFile : decompiledOutputFileSet)
                {
                    FileModel decompiledFileModel = fileService.getByUniqueProperty(
                                FileModel.PROPERTY_FILE_PATH, decompiledOutputFile);

                    if (decompiledOutputFile.endsWith(".java"))
                    {
                        if (decompiledFileModel == null)
                        {
                            decompiledFileModel = event.getGraphContext().getFramed()
                                        .addVertex(null, FileModel.class);
                            decompiledFileModel.setFilePath(decompiledOutputFile);
                        }

                        Path classFilepath = Paths.get(decompiledOutputFile.substring(0,
                                    decompiledOutputFile.length() - 5)
                                    + ".class");
                        FileModel classFileModel = fileService.getByUniqueProperty(
                                    FileModel.PROPERTY_FILE_PATH, classFilepath);
                        if (classFileModel != null && classFileModel instanceof JavaClassModel)
                        {
                            JavaClassModel classModel = (JavaClassModel) classFileModel;
                            classModel.setDecompiledSource(decompiledFileModel);
                        }
                    }
                    payload.addDecompiledFileModel(decompiledFileModel);
                }
            }
            catch (final DecompilationException exc)
            {
                throw new WindupException("Error decompiling archive " + archivePath + " due to: " + exc.getMessage(),
                            exc);
            }
        }
    }
}