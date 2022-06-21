package org.jboss.windup.rules.apps.java.reporting.freemarker.filepath;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;

import java.io.File;

/**
 * Used for compatible file report in order to distinguish .class files and .java files.
 *
 * <p>
 * Called as follows:
 * <p>
 * getPrettyPathForFileWithExtensions(fileModel)
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class GetPrettyPathForFileWithExtensions extends AbstractGetPrettyPathForFile {
    public String getPath(JavaClassFileModel jcfm) {
        String filename = jcfm.getFileName();
        String packageName = jcfm.getPackageName() == null ? "" : jcfm.getPackageName().replaceAll("\\.", File.separator);
        String qualifiedName = packageName + File.separator + filename;
        String reportFileName = qualifiedName;
        return reportFileName;
    }

    public String getPath(JavaSourceFileModel javaSourceModel) {
        String filename = javaSourceModel.getFileName();
        String packageName = javaSourceModel.getPackageName() == null ? "" : javaSourceModel.getPackageName().replaceAll("\\.", File.separator);
        String qualifiedName = packageName + File.separator + filename;
        String reportFileName = qualifiedName;
        return reportFileName;
    }

    public String getPath(ReportResourceFileModel model) {
        return "resources/" + model.getPrettyPath();
    }

    public String getPath(FileModel model) {
        return model.getPrettyPathWithinProject();
    }
}
