package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Returns a pretty path for the provided file. If the File appears to represent a Java File, this will attempt to determine the associated Java Class
 * and return the name formatted as a package and class (eg, com.package.Foo).
 * 
 * Called as follows:
 * 
 * getPrettyPathForFile(fileModel)
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class GetPrettyPathForFile implements WindupFreeMarkerMethod
{
    private static final String NAME = "getPrettyPathForFile";

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        try
        {
            if (arguments.size() != 1)
            {
                throw new TemplateModelException("Error, method expects one argument (ResourceModel)");
            }
            StringModel stringModelArg = (StringModel) arguments.get(0);
            ResourceModel fileModel = (ResourceModel) stringModelArg.getWrappedObject();
            if (fileModel instanceof JavaClassFileModel)
            {
                JavaClassFileModel jcfm = (JavaClassFileModel) fileModel;
                return jcfm.getJavaClass().getQualifiedName();
            }
            else if (fileModel instanceof JavaSourceFileModel)
            {
                JavaSourceFileModel javaSourceModel = (JavaSourceFileModel) fileModel;
                String filename = fileModel.getFileName();
                String packageName = javaSourceModel.getPackageName();

                if (filename.endsWith(".java"))
                {
                    filename = filename.substring(0, filename.length() - 5);
                }

                return packageName == null || packageName.equals("") ? filename : packageName + "." + filename;
            }
            else
            {
                return fileModel.getPrettyPathWithinProject();
            }
        }
        finally
        {
            ExecutionStatistics.get().end(NAME);
        }
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + ResourceModel.class.getSimpleName()
                    + " as a parameter and either the qualified name (if it is a Java file) or the path within the file's project.";
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        // no-op
    }

}
