package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.FileInputStream;
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
import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
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
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        try
        {
            try (FileInputStream fis = new FileInputStream(payload.getFilePath()))
            {
                final ClassParser parser = new ClassParser(fis, payload.getFilePath());
                final JavaClass bcelJavaClass = parser.parse();
                final String packageName = bcelJavaClass.getPackageName();
                final String qualifiedName = bcelJavaClass.getClassName();
                int majorVersion = bcelJavaClass.getMajor();
                int minorVersion = bcelJavaClass.getMinor();

                String simpleName = qualifiedName;
                if (packageName != null && !packageName.equals("") && simpleName != null)
                {
                    simpleName = simpleName.substring(packageName.length() + 1);
                }

                final JavaClassFileModel classFileModel = GraphService.addTypeToModel(event.getGraphContext(),
                            payload, JavaClassFileModel.class);

                classFileModel.setMajorVersion(majorVersion);
                classFileModel.setMinorVersion(minorVersion);
                classFileModel.setPackageName(packageName);

                final JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                final JavaClassModel javaClassModel = javaClassService.getOrCreate(qualifiedName);

                javaClassModel.setSimpleName(simpleName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
                javaClassModel.setClassFile(classFileModel);

                final String[] interfaceNames = bcelJavaClass.getInterfaceNames();
                if (interfaceNames != null)
                {
                    for (final String iface : interfaceNames)
                    {
                        JavaClassModel interfaceModel = javaClassService.getOrCreate(iface);
                        javaClassModel.addImplements(interfaceModel);
                    }
                }

                for (final Method method : bcelJavaClass.getMethods())
                {
                    javaClassService.addJavaMethod(javaClassModel, method.getName(),
                                toJavaClasses(javaClassService, method.getArgumentTypes()));
                }

                final Constant[] pool = bcelJavaClass.getConstantPool().getConstantPool();
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

                String superclassName = bcelJavaClass.getSuperclassName();
                if (Strings.isNullOrEmpty(superclassName))
                    javaClassModel.setExtends(javaClassService.getOrCreate(superclassName));

                classFileModel.setJavaClass(javaClassModel);
            }
        }
        catch (Exception e)
        {
            LOG.log(Level.WARNING,
                        "BCEL was unable to parse class file: " + payload.getFilePath() + " due to: " + e.getMessage(),
                        e);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(payload, JavaClassFileModel.UNPARSEABLE_CLASS_CLASSIFICATION,
                        JavaClassFileModel.UNPARSEABLE_CLASS_DESCRIPTION);
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
