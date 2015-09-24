package org.jboss.windup.rules.apps.java.decompiler;

import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;

/**
 * An abstract operation providing some default methods useful for the DecompilerOperations.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
abstract public class AbstractDecompilerOperation extends GraphOperation
{
    private Iterable<JavaClassFileModel> filesToDecompile;

    Iterable<JavaClassFileModel> getFilesToDecompile(GraphContext context)
    {
        if (this.filesToDecompile == null)
        {
            filesToDecompile= getDefaultFilesToDecompile(context);
        }
        return filesToDecompile;
    }

    /**
     * Method that will set files that should be decompiled when the operation will operate. In case any are specified, the operation will take
     * all .class files that were not marked to be skipped.
     * @param filesToDecompile
     */
    public void setFilesToDecompile(Iterable<JavaClassFileModel> filesToDecompile)
    {
        this.filesToDecompile = filesToDecompile;
    }

    private Iterable<JavaClassFileModel> getDefaultFilesToDecompile(GraphContext context) {
        GraphService<JavaClassFileModel> classFileService = new GraphService<>(context, JavaClassFileModel.class);
        return classFileService.findAllByWithoutProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
    }

}
