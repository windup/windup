package org.jboss.windup.rules.apps.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.files.FileDiscoveredEvent;
import org.jboss.windup.rules.files.FileDiscoveredListener;
import org.jboss.windup.rules.files.FileDiscoveredResult;
import org.jboss.windup.util.Logging;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassFileListener implements FileDiscoveredListener
{
    private static Logger LOG = Logging.get(ClassFileListener.class);

    @Override
    public FileDiscoveredResult fileDiscovered(FileDiscoveredEvent event)
    {
        if (!isClassFile(event))
            return FileDiscoveredResult.CONTINUE;

        // keep inner classes (we may need them for decompilation purposes)
        if (event.getFilename().contains("$"))
            return FileDiscoveredResult.KEEP;

        try (InputStream is = event.getInputStream())
        {
            final ClassParser parser = new ClassParser(is, event.getFilename());
            final JavaClass javaClass = parser.parse();
            DependencyAnalyzer analyzer = new DependencyAnalyzer(javaClass);
            if (analyzer.shouldKeep)
                return FileDiscoveredResult.KEEP;
            else
                return FileDiscoveredResult.DISCARD;
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING, "Failed to analyze class file: " + event.getFilename() + " due to: " + e.getMessage(), e);
            return FileDiscoveredResult.KEEP;
        }
    }

    private boolean isClassFile(FileDiscoveredEvent event)
    {
        return event.getFilename().toLowerCase().endsWith(".class");
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

            ConstantPool cp = javaClass.getConstantPool();
            String typeReference = obj.getBytes(cp);
            typeReference = typeReference.replace('/', '.').replace('\\', '.');
            if (TypeInterestFactory.matchesAny(typeReference, null))
                this.shouldKeep = true;
        }

    }

}
