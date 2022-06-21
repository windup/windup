package org.jboss.windup.rules.apps.java.decompiler;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.util.PathUtil;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DecompilerUtil {
    /**
     * Returns an appropriate output directory for the decompiled data based upon the provided {@link JavaClassFileModel}.
     * <p>
     * This should be the top-level directory for the package (eg, /tmp/project/foo for the file /tmp/project/foo/com/example/Foo.class).
     * <p>
     * This could be the same directory as the file itself, if the file is already in the output directory. If the .class file is referencing a file
     * in the input directory, then this will be a classes folder underneath the output directory.
     */
    static File getOutputDirectoryForClass(GraphContext context, JavaClassFileModel fileModel) {
        final File result;
        WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(context);
        File inputPath = fileModel.getProjectModel().getRootProjectModel().getRootFileModel().asFile();
        if (PathUtil.isInSubDirectory(inputPath, fileModel.asFile())) {
            String outputPath = configuration.getOutputPath().getFilePath();
            result = Paths.get(outputPath).resolve("classes").toFile();
        } else {

            String packageName = fileModel.getPackageName();
            if (StringUtils.isBlank(packageName))
                return fileModel.asFile().getParentFile();

            String[] packageComponents = packageName.split("\\.");
            File rootFile = fileModel.asFile().getParentFile();
            for (int i = 0; i < packageComponents.length; i++) {
                rootFile = rootFile.getParentFile();
            }
            result = rootFile;
        }
        return result;
    }
}
