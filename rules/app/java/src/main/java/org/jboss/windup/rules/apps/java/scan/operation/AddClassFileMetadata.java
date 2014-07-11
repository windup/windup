package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.FileInputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaFileModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AddClassFileMetadata extends AbstractIterationOperation<FileModel>
{
    private AddClassFileMetadata(String variableName)
    {
        super(FileModel.class, variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
    {
        try
        {
            try (FileInputStream fis = new FileInputStream(payload.getFilePath()))
            {
                ClassParser parser = new ClassParser(fis, payload.getFilePath());
                JavaClass javaClass = parser.parse();
                String packageName = javaClass.getPackageName();
                String qualifiedName = javaClass.getClassName();

                String simpleName = qualifiedName;
                if (packageName != null && !packageName.equals("") && simpleName != null)
                {
                    simpleName = simpleName.substring(packageName.length() + 1);
                }

                JavaFileModel classFileModel = GraphService.addTypeToModel(event.getGraphContext(),
                            payload, JavaFileModel.class);

                classFileModel.setPackageName(packageName);

                GraphService<JavaClassModel> javaClassModelService = new GraphService<>(event.getGraphContext(),
                            JavaClassModel.class);
                JavaClassModel javaClassModel = javaClassModelService.create();
                javaClassModel.setSimpleName(simpleName);
                javaClassModel.setPackageName(packageName);
                javaClassModel.setQualifiedName(qualifiedName);
                javaClassModel.setClassFile(classFileModel);

                for (JavaClass iface : javaClass.getAllInterfaces())
                {
                    JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                    JavaClassModel interfaceModel = javaClassService.getOrCreate(iface.getClassName());
                    javaClassModel.addImplements(interfaceModel);
                }

                // TODO add more metadata about supertypes, etc.

                classFileModel.addJavaClass(javaClassModel);
            }
        }
        catch (Exception e)
        {
            throw new WindupException("Error getting class information for " + payload.getFilePath() + " due to: "
                        + e.getMessage(),
                        e);
        }
    }

    public static OperationBuilder to(String var)
    {
        return new AddClassFileMetadata(var);
    }

}
