package org.jboss.windup.rules.apps.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestResolver;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.rules.files.FileDiscoveredEvent;
import org.jboss.windup.rules.files.FileDiscoveredListener;
import org.jboss.windup.rules.files.FileDiscoveredResult;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.objectweb.asm.ClassReader;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassFileListener implements FileDiscoveredListener
{
    private static Logger LOG = Logging.get(ClassFileListener.class);

    @Override
    public void fileModelCreated(GraphRewrite event, EvaluationContext context, FileModel fileModel)
    {
        ExecutionStatistics.get().begin("ClassFileListener.fileModelCreated()");
        try
        {
            if (fileModel.isDirectory())
                return;

            if (!isClassFile(fileModel.getFileName()))
                return;

            try (InputStream is = fileModel.asInputStream())
            {
                ClassReader classReader = new ClassReader(is);

                addClassFileMetadata(event, context, fileModel);

                // keep inner classes (we may need them for decompilation purposes)
                if (fileModel.getFileName().contains("$"))
                    return;

                WindupJavaConfigurationService configurationService = new WindupJavaConfigurationService(event.getGraphContext());
                if (!configurationService.shouldScanFile(fileModel.getFilePath()))
                {
                    fileModel.asVertex().setProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
                    return;
                }

                DependencyVisitor dependencyVisitor = new DependencyVisitor();

                classReader.accept(dependencyVisitor, 0);
                for (String typeReference : dependencyVisitor.classes)
                {
                    if (shouldKeep(typeReference))
                        return;
                }

                fileModel.asVertex().setProperty(JavaClassFileModel.SKIP_DECOMPILATION, true);
            }
            catch (IOException e)
            {
                LOG.log(Level.WARNING,
                            "ASM was unable to parse class file: " + fileModel.getFilePath() + " due to: " + e.getMessage(),
                            e);
                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                classificationService.attachClassification(context, fileModel, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                            JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
                return;
            }
        }
        finally
        {
            ExecutionStatistics.get().end("ClassFileListener.fileModelCreated()");
        }
    }

    private void addClassFileMetadata(GraphRewrite event, EvaluationContext context, FileModel fileModel)
    {
        try (FileInputStream fis = new FileInputStream(fileModel.getFilePath()))
        {
            final ClassParser parser = new ClassParser(fis, fileModel.getFilePath());
            final JavaClass bcelJavaClass = parser.parse();
            final String packageName = bcelJavaClass.getPackageName();

            final String qualifiedName = bcelJavaClass.getClassName();

            final JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
            final JavaClassModel javaClassModel = javaClassService.create(qualifiedName);
            int majorVersion = bcelJavaClass.getMajor();
            int minorVersion = bcelJavaClass.getMinor();

            String simpleName = qualifiedName;
            if (packageName != null && !packageName.equals("") && simpleName != null)
            {
                simpleName = StringUtils.substringAfterLast(simpleName, ".");
            }

            JavaClassFileModel javaClassFileModel = new GraphService<>(event.getGraphContext(), JavaClassFileModel.class).addTypeToModel(fileModel);
            javaClassFileModel.setMajorVersion(majorVersion);
            javaClassFileModel.setMinorVersion(minorVersion);
            javaClassFileModel.setPackageName(packageName);

            javaClassModel.setSimpleName(simpleName);
            javaClassModel.setPackageName(packageName);
            javaClassModel.setQualifiedName(qualifiedName);
            javaClassModel.setClassFile(javaClassFileModel);
            javaClassModel.setPublic(bcelJavaClass.isPublic());
            javaClassModel.setInterface(bcelJavaClass.isInterface());

            final String[] interfaceNames = bcelJavaClass.getInterfaceNames();
            if (interfaceNames != null)
            {
                for (final String interfaceName : interfaceNames)
                {
                    JavaClassModel interfaceModel = javaClassService.getOrCreatePhantom(interfaceName);
                    javaClassService.addInterface(javaClassModel, interfaceModel);
                }
            }

            String superclassName = bcelJavaClass.getSuperclassName();
            if (!bcelJavaClass.isInterface() && !StringUtils.isBlank(superclassName))
                javaClassModel.setExtends(javaClassService.getOrCreatePhantom(superclassName));

            javaClassFileModel.setJavaClass(javaClassModel);
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING,
                        "BCEL was unable to parse class file: " + fileModel.getFilePath() + " due to: " + e.getMessage(),
                        e);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(context, fileModel, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                        JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
        }
    }

    private boolean shouldKeep(String typeReference)
    {
        typeReference = typeReference.replace('/', '.').replace('\\', '.');

        int lastDot = typeReference.lastIndexOf(".");
        String packageName = lastDot == -1 ? "" : typeReference.substring(0, lastDot);
        String className = lastDot == -1 ? typeReference : typeReference.substring(lastDot + 1);

        return TypeInterestResolver.defaultInstance().isInteresting(packageName, className, null);
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
