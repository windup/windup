package org.jboss.windup.rules.apps.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.rules.files.FileDiscoveredEvent;
import org.jboss.windup.rules.files.FileDiscoveredListener;
import org.jboss.windup.rules.files.FileDiscoveredResult;
import org.jboss.windup.util.Logging;
import org.objectweb.asm.ClassReader;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassFileListener implements FileDiscoveredListener
{
    private static Logger LOG = Logging.get(ClassFileListener.class);

    @Override
    public void fileModelCreated(GraphContext context, FileModel fileModel)
    {
        if (fileModel.isDirectory())
            return;

        if (!isClassFile(fileModel.getFileName()))
            return;

        // keep inner classes (we may need them for decompilation purposes)
        if (fileModel.getFileName().contains("$"))
            return;

        WindupJavaConfigurationService configurationService = new WindupJavaConfigurationService(context);
        if (!configurationService.shouldScanFile(fileModel.getFilePath()))
        {
            fileModel.asVertex().setProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
            return;
        }

        try (InputStream is = fileModel.asInputStream())
        {
            DependencyVisitor v = new DependencyVisitor();
            ClassReader cr = new ClassReader(is);
            cr.accept(v, 0);
            for (String className : v.classes)
            {
                if (shouldKeep(className))
                    return;
            }

            fileModel.asVertex().setProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING, "Failed to analyze class file: " + fileModel.getFileName() + " due to: " + e.getMessage(), e);
            return;
        }
    }

    private boolean shouldKeep(String typeReference)
    {
        typeReference = typeReference.replace('/', '.').replace('\\', '.');
        return (TypeInterestFactory.matchesAny(typeReference, null));
    }

    @Override
    public FileDiscoveredResult fileDiscovered(FileDiscoveredEvent event)
    {
        return FileDiscoveredResult.CONTINUE;
    }

    private boolean isClassFile(String filename)
    {
        return filename.toLowerCase().endsWith(".class");
    }

    private class DependencyAnalyzer extends EmptyVisitor
    {
        private JavaClass javaClass;
        private boolean shouldKeep = false;

        public DependencyAnalyzer(JavaClass javaClass)
        {
            this.javaClass = javaClass;
            DescendingVisitor classWalker = new DescendingVisitor(javaClass, this);
            classWalker.visit();
        }

        @Override
        public void visitConstantClass(ConstantClass obj)
        {
            // already marked as a keeper, just skip
            if (this.shouldKeep)
                return;


        }

    }

}
