package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds metadata from the .class file itself to the graph.
 * 
 */
public class AddClassFileMetadata extends AbstractIterationOperation<JavaClassFileModel>
{
    private static Logger LOG = Logger.getLogger(AddClassFileMetadata.class.getSimpleName());

    private AddClassFileMetadata(String variableName)
    {
        super(variableName);
    }

    public AddClassFileMetadata()
    {
        super();
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, JavaClassFileModel payload)
    {
        ExecutionStatistics.get().begin("AddClassFileMetadata.perform()");
        try
        {
            WindupJavaConfigurationModel javaConfiguration = WindupJavaConfigurationService.getJavaConfigurationModel(event.getGraphContext());

            String absolutePath = payload.asFile().getAbsolutePath();

            if (!new WindupJavaConfigurationService(event.getGraphContext()).shouldScanFile(absolutePath))
                return;

            // we should scan it, so make sure we get the package name from it
            try (FileInputStream fis = new FileInputStream(payload.getFullPath()))
            {
                final ClassParser parser = new ClassParser(fis, payload.getFullPath());
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
                    simpleName = StringUtils.removeStart(simpleName, packageName);
                }

                payload.setMajorVersion(majorVersion);
                payload.setMinorVersion(minorVersion);
                payload.setPackageName(packageName);

                javaClassModel.setSimpleName(simpleName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
                javaClassModel.setClassFile(payload);
                javaClassModel.setPublic(bcelJavaClass.isPublic());

                final String[] interfaceNames = bcelJavaClass.getInterfaceNames();
                if (interfaceNames != null)
                {
                    for (final String interfaceName : interfaceNames)
                    {
                        JavaClassModel interfaceModel = javaClassService.getOrCreatePhantom(interfaceName);
                        javaClassModel.addImplements(interfaceModel);
                    }
                }

                String superclassName = bcelJavaClass.getSuperclassName();
                if (!StringUtils.isBlank(superclassName))
                    javaClassModel.setExtends(javaClassService.getOrCreatePhantom(superclassName));

                payload.setJavaClass(javaClassModel);
            }
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING,
                        "BCEL was unable to parse class file: " + payload.getFullPath() + " due to: " + e.getMessage(),
                        e);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(payload, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                        JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
        }
        finally
        {
            ExecutionStatistics.get().end("AddClassFileMetadata.perform()");
        }
    }

    private JavaClassModel[] toJavaClasses(final JavaClassService javaClassService, final Type[] types)
    {
        JavaClassModel[] clz = new JavaClassModel[types.length];

        for (int i = 0, j = types.length; i < j; i++)
        {
            Type t = types[i];
            clz[i] = javaClassService.getOrCreatePhantom(t.toString());
        }

        return clz;
    }

    public static OperationBuilder to(String var)
    {
        return new AddClassFileMetadata(var);
    }

}
