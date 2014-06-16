package org.jboss.windup.rules.apps.java.scan.op;

import java.io.FileInputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class AddClassFileMetadata extends AbstractIterationOperator<FileModel>
{

    public AddClassFileMetadata(String variableName)
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
                String classQualifiedName = javaClass.getClassName();
                String className = classQualifiedName;
                if (packageName != null && !packageName.equals("") && className != null)
                {
                    // remove package name so that we just have the classname by itself
                    className = className.substring(packageName.length() + 1);
                }

                JavaClassModel classModel = GraphUtil.addTypeToModel(event.getGraphContext(),
                            payload, JavaClassModel.class);
                classModel.setPackageName(packageName);
                classModel.setQualifiedName(classQualifiedName);
                classModel.setClassName(className);
                classModel.setClassFile(payload);
            }
        }
        catch (Exception e)
        {
            throw new WindupException("Error getting class information for " + payload.getFilePath() + " due to: "
                        + e.getMessage(),
                        e);
        }
    }

}
