package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.FileInputStream;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.executor.WindupExecutorFactory;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileModelService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds metadata from the .class file itself to the graph.
 * 
 */
public class AddClassFileMetadata extends AbstractIterationOperation<FileModel>
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
    public void perform(final GraphRewrite event, final EvaluationContext context, final FileModel inputPayload)
    {
        ExecutionStatistics.get().begin("AddClassFileMetadata.perform()");
        try
        {

            try (FileInputStream fis = new FileInputStream(inputPayload.getFilePath()))
            {
                final ClassParser parser = new ClassParser(fis, inputPayload.getFilePath());
                final JavaClass bcelJavaClass = parser.parse();
                final String packageName = bcelJavaClass.getPackageName();
                final String qualifiedName = bcelJavaClass.getClassName();
                final int majorVersion = bcelJavaClass.getMajor();
                final int minorVersion = bcelJavaClass.getMinor();

                final String simpleName;
                if (packageName != null && !packageName.equals("") && qualifiedName != null)
                {
                    simpleName = qualifiedName.substring(packageName.length() + 1);
                }
                else
                {
                    simpleName = qualifiedName;
                }

                Callable<Void> persistence = new Callable<Void>()
                {
                    private int numberOfCalls = 0;

                    @Override
                    public Void call() throws Exception
                    {
                        numberOfCalls++;
                        if (numberOfCalls % 10 == 0)
                        {
                            event.getGraphContext().commit();
                        }
                        // reload to make sure it is from this transaction
                        FileModel payload = new FileModelService(event.getGraphContext()).getById(inputPayload.asVertex().getId());

                        final JavaClassFileModel classFileModel = GraphService.addTypeToModel(event.getGraphContext(),
                                    payload, JavaClassFileModel.class);

                        classFileModel.setMajorVersion(majorVersion);
                        classFileModel.setMinorVersion(minorVersion);
                        classFileModel.setPackageName(packageName);
                        WindupJavaConfigurationService javaCfgService = new WindupJavaConfigurationService(event.getGraphContext());
                        final String[] interfaceNames = bcelJavaClass.getInterfaceNames();
                        final String superclassName = bcelJavaClass.getSuperclassName();
                        final Constant[] pool = bcelJavaClass.getConstantPool().getConstantPool();
                        final Method[] methods = bcelJavaClass.getMethods();

                        final JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                        final JavaClassModel javaClassModel;
                        javaClassModel = javaClassService.getOrCreate(qualifiedName);
                        javaClassModel.setSimpleName(simpleName);
                        javaClassModel.setPackageName(packageName);
                        javaClassModel.setQualifiedName(qualifiedName);
                        javaClassModel.setClassFile(classFileModel);
                        if (interfaceNames != null)
                        {
                            for (final String iface : interfaceNames)
                            {
                                JavaClassModel interfaceModel = javaClassService.getOrCreate(iface);
                                javaClassModel.addImplements(interfaceModel);
                            }
                        }

                        if (!StringUtils.isBlank(superclassName))
                            javaClassModel.setExtends(javaClassService.getOrCreate(superclassName));

                        if (javaCfgService.shouldScanPackage(packageName)) // only add these details if this is a scanned package
                        {
                            for (final Method method : methods)
                            {
                                javaClassService.addJavaMethod(javaClassModel, method.getName(),
                                            toJavaClasses(javaClassService, method.getArgumentTypes()));
                            }

                            for (final Constant c : pool)
                            {
                                if (c == null)
                                    continue;
                                c.accept(new EmptyVisitor()
                                {
                                    @Override
                                    public void visitConstantClass(final ConstantClass obj)
                                    {
                                        final ConstantPool pool = bcelJavaClass.getConstantPool();
                                        String classVal = obj.getConstantValue(pool).toString();
                                        classVal = StringUtils.replace(classVal, "/", ".");

                                        if (StringUtils.equals(classVal, bcelJavaClass.getClassName()))
                                        {
                                            // skip adding class name.
                                            return;
                                        }

                                        final JavaClassModel clz = javaClassService.getOrCreate(classVal);
                                        javaClassModel.addImport(clz);
                                    }
                                });
                            }
                        }
                        classFileModel.setJavaClass(javaClassModel);
                        return null;
                    }
                };

                WindupExecutorFactory.getSingleThreadedIterationPersistenceExecutor(event).submit(persistence);
            }
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING,
                        "BCEL was unable to parse class file: " + inputPayload.getFilePath() + " due to: " + e.getMessage(),
                        e);
            synchronized (event.getGraphContext())
            {
                ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                classificationService.attachClassification(inputPayload, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                            JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
            }
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
            clz[i] = javaClassService.getOrCreate(t.toString());
        }

        return clz;
    }

    public static OperationBuilder to(String var)
    {
        return new AddClassFileMetadata(var);
    }

}
