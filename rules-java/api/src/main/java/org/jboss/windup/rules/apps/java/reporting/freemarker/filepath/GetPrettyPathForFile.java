package org.jboss.windup.rules.apps.java.reporting.freemarker.filepath;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;

import java.util.ArrayList;

/**
 * Returns a pretty path for the provided file. If the File appears to represent a Java File, this will attempt to determine the associated Java Class
 * and return the name formatted as a package and class (eg, com.package.Foo).
 * <p>
 * Called as follows:
 * <p>
 * getPrettyPathForFile(fileModel)
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetPrettyPathForFile extends AbstractGetPrettyPathForFile
{
    public String getPath(JavaClassFileModel jcfm)
    {
        return jcfm.getJavaClass().getQualifiedName();
    }

    public String getPath(ReportResourceFileModel model)
    {
        return "resources/" + model.getPrettyPath();
    }

    public String getPath(FileModel model)
    {
        return model.getPrettyPathWithinProject();
    }

    public String getPath(JavaSourceFileModel javaSourceModel)
    {
        String filename = javaSourceModel.getFileName();
        String packageName = javaSourceModel.getPackageName();

        if (filename.endsWith(".java"))
        {
            filename = filename.substring(0, filename.length() - 5);
        }

        return packageName == null || packageName.equals("") ? filename : packageName + "." + filename;
    }

    public static void addPrettyPathToModel(FileModel fileModel) {
        if (fileModel == null) {
            return;
        }

        ArrayList<String> types = fileModel.asVertex().getProperty(WindupVertexFrame.TYPE_PROP);

        if (types.contains(JavaClassFileModel.TYPE)) {
            //if (fileModel instanceof JavaClassFileModel) {
            JavaClassFileModel jcfm = ((JavaClassFileModel) fileModel);
            jcfm.setCachedPrettyPath(jcfm.getPrettyPathWithinProject(true));
            //} else if (fileModel instanceof ReportResourceFileModel) {
        } else if (types.contains(JavaSourceFileModel.TYPE)) {
            JavaSourceFileModel jsfm = ((JavaSourceFileModel)fileModel);
            jsfm.setCachedPrettyPath(jsfm.getPrettyPathWithinProject(true));
        } else if (types.contains(ReportResourceFileModel.TYPE)) {
            ReportResourceFileModel rrfm = (ReportResourceFileModel)fileModel;
            rrfm.setCachedPrettyPath(rrfm.getPrettyPathWithinProject(false));
        } else {
            fileModel.setCachedPrettyPath(fileModel.getPrettyPathWithinProject(false));
        }
    }
}
