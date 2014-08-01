package org.jboss.windup.rules.apps.javadecompiler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.decompiler.api.DecompilationException;
import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.procyon.ProcyonConfiguration;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;
import org.jboss.windup.graph.dao.FileModelService;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ProcyonDecompilerOperation extends AbstractIterationOperation<ArchiveModel>
{

    public ProcyonDecompilerOperation(String variableName)
    {
        super(ArchiveModel.class, variableName);
    }
    
    public ProcyonDecompilerOperation()
    {
        super(ArchiveModel.class);
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

                FileModelService fileService = new FileModelService(event.getGraphContext());
                for (String decompiledOutputFile : decompiledOutputFileSet)
                {
                    FileModel decompiledFileModel = fileService.getUniqueByProperty(FileModel.PROPERTY_FILE_PATH,
                                decompiledOutputFile);

                    if (decompiledFileModel == null)
                    {
                        FileModel parentFileModel = fileService.findByPath(Paths.get(decompiledOutputFile)
                                    .getParent()
                                    .toString());
                        decompiledFileModel = fileService.createByFilePath(parentFileModel, decompiledOutputFile);
                        decompiledFileModel.setParentArchive(payload);
                    }
                    ProjectModel projectModel = payload.getProjectModel();
                    decompiledFileModel.setProjectModel(projectModel);
                    projectModel.addFileModel(decompiledFileModel);

                    if (decompiledOutputFile.endsWith(".java"))
                    {

                        if (!(decompiledFileModel instanceof JavaSourceFileModel))
                        {
                            decompiledFileModel = GraphService.addTypeToModel(event.getGraphContext(),
                                        decompiledFileModel, JavaSourceFileModel.class);
                        }
                        JavaSourceFileModel decompiledSourceFileModel = (JavaSourceFileModel) decompiledFileModel;
                        decompiledSourceFileModel.setPackageName(decompiledOutputFile);

                        Path classFilepath = Paths.get(decompiledOutputFile.substring(0,
                                    decompiledOutputFile.length() - 5)
                                    + ".class");
                        FileModel classFileModel = fileService.getUniqueByProperty(
                                    FileModel.PROPERTY_FILE_PATH, classFilepath);
                        if (classFileModel != null && classFileModel instanceof JavaClassModel)
                        {
                            JavaClassModel classModel = (JavaClassModel) classFileModel;
                            classModel.setDecompiledSource(decompiledSourceFileModel);

                            decompiledSourceFileModel.setPackageName(classModel.getPackageName());
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