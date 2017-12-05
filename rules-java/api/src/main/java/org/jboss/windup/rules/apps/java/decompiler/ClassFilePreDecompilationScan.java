package org.jboss.windup.rules.apps.java.decompiler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.ClassFileScanner;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.java.DependencyVisitor;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.rules.apps.java.scan.ast.ignore.JavaClassIgnoreResolver;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.ProgressEstimate;
import org.jboss.windup.util.Util;
import org.objectweb.asm.ClassReader;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * An operation doing a pre-scan of the .class file in order to check if it is possible to tell in advance if it is worth decompiling the class.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author Ondrej Zizka
 */
public class ClassFilePreDecompilationScan extends GraphOperation
{
    private static final Logger LOG = Logging.get(ClassFilePreDecompilationScan.class);

    String UNPARSEABLE_CLASS_CLASSIFICATION = "Unparseable Class File";
    String UNPARSEABLE_CLASS_DESCRIPTION = "This Class file could not be parsed";

    private void addClassFileMetadata(GraphRewrite event, EvaluationContext context, JavaClassFileModel javaClassFileModel)
    {
        try (FileInputStream fis = new FileInputStream(javaClassFileModel.getFilePath()))
        {
            final ClassParser parser = new ClassParser(fis, javaClassFileModel.getFilePath());
            final JavaClass bcelJavaClass = parser.parse();
            final String packageName = bcelJavaClass.getPackageName();

            final String qualifiedName = bcelJavaClass.getClassName();

            final JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
            final JavaClassModel javaClassModel = javaClassService.create(qualifiedName);
            int majorVersion = bcelJavaClass.getMajor();
            int minorVersion = bcelJavaClass.getMinor();

            String simpleName = qualifiedName;
            if (packageName != null && !packageName.isEmpty() && simpleName != null)
            {
                simpleName = StringUtils.substringAfterLast(simpleName, ".");
            }

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
        catch (Exception ex)
        {
            String nl = ex.getMessage() != null ? Util.NL + "\t" : " ";
            final String message = "BCEL was unable to parse class file '" + javaClassFileModel.getFilePath() + "':" + nl + ex.toString();
            LOG.log(Level.WARNING, message);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(event, context, javaClassFileModel, UNPARSEABLE_CLASS_CLASSIFICATION,
                        UNPARSEABLE_CLASS_DESCRIPTION);
            javaClassFileModel.setParseError(message);
            javaClassFileModel.setSkipDecompilation(true);
        }
    }

    private void filterClassesToDecompile(GraphRewrite event, EvaluationContext context, JavaClassFileModel fileModel)
    {
        if (fileModel.getSkipDecompilation() != null && fileModel.getSkipDecompilation())
            return;

        try (InputStream is = fileModel.asInputStream())
        {
            WindupJavaConfigurationService configurationService = new WindupJavaConfigurationService(event.getGraphContext());
            boolean shouldScan;
            if (fileModel.getPackageName() != null)
                shouldScan = configurationService.shouldScanPackage(fileModel.getPackageName());
            else
                shouldScan = configurationService.shouldScanFile(fileModel.getFilePath());

            if (!shouldScan)
            {
                LOG.fine("Skipping decompilation for: " + fileModel.getFilePath() + " due to configuration!");
                fileModel.setSkipDecompilation(true);
                return;
            }

            // keep inner classes (we may need them for decompilation purposes)
            if (fileModel.getFileName().contains("$"))
                return;

            DependencyVisitor dependencyVisitor = new DependencyVisitor();
            ClassReader classReader = new ClassReader(is);
            classReader.accept(dependencyVisitor, 0);

            // If we should ignore any of the contained classes, skip decompilation of the whole file.
            for (String typeReference : dependencyVisitor.classes)
            {
                if (shouldIgnore(typeReference))
                {
                    LOG.fine("Skipping decompilation for: " + fileModel.getFilePath() + " due javaclass-ignore!");
                    fileModel.setSkipDecompilation(true);
                    break;
                }
            }
        }
        catch (IOException | IllegalArgumentException e)
        {
            final String message = "ASM was unable to parse class file '" + fileModel.getFilePath() + "':\n\t" + e.getMessage();
            LOG.log(Level.WARNING, message, e);
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            classificationService.attachClassification(event, context, fileModel, UNPARSEABLE_CLASS_CLASSIFICATION, UNPARSEABLE_CLASS_DESCRIPTION);
            fileModel.setParseError(message);
        }
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        ExecutionStatistics.get().begin("ClassFilePreDecompilationScan.perform()");
        try
        {
            WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(event.getGraphContext());
            Set<String> classpaths = new HashSet<>();
            for (FileModel inputPath : configuration.getInputPaths())
            {
                if (inputPath.isDirectory())
                    classpaths.add(inputPath.getFilePath());
            }
            ArchiveService archiveService = new ArchiveService(event.getGraphContext());
            for (ArchiveModel archiveModel : archiveService.findAll())
            {
                classpaths.add(archiveModel.getFilePath());
            }
            ClassFileScanner classFileScanner = new ClassFileScanner(classpaths);

            GraphService<JavaClassFileModel> javaClassFileService = new GraphService<>(event.getGraphContext(), JavaClassFileModel.class);
            ClassFileMap classFileMap = new ClassFileMap();

            int totalWork = 0;
            for (JavaClassFileModel javaClassFileModel : javaClassFileService.findAllWithoutProperty(FileModel.PARSE_ERROR))
            {
                classFileMap.addClass(javaClassFileModel);
                totalWork++;
            }
            ProgressEstimate progressEstimate = new ProgressEstimate(totalWork);

            for (List<JavaClassFileModel> classBatch : classFileMap.getClasses())
            {
                try
                {
                    classBatch.sort(Comparator.comparingInt(o -> o.getFileName().length()));

                    boolean foundMatch = false;
                    for (JavaClassFileModel fileModel : classBatch)
                    {
                        addClassFileMetadata(event, context, fileModel);
                    }

                    for (JavaClassFileModel fileModel : classBatch)
                    {
                        filterClassesToDecompile(event, context, fileModel);
                        if (fileModel.getParseError() != null)
                            continue;

                        Collection<ClassReference> references = classFileScanner.scanClass(Paths.get(fileModel.getFilePath()));
                        Map<String, ClassReference> deduplicatedReferences = new HashMap<>();
                        for (ClassReference classReference : references)
                        {
                            String key = classReference.getLocation() + "_" + classReference.getQualifiedName();
                            if (!deduplicatedReferences.containsKey(key))
                                deduplicatedReferences.put(key, classReference);

                            // Also, include an import line for each qualified name
                            String importQualifiedName = StringUtils.isNotBlank(classReference.getPackageName()) ? classReference.getPackageName() + "." : "";

                            // For import purposes, do not include the array markers
                            importQualifiedName += classReference.getClassName().replace("[", "").replace("]", "");
                            key = TypeReferenceLocation.IMPORT + "_" + importQualifiedName;

                            if (!deduplicatedReferences.containsKey(key))
                                deduplicatedReferences.put(key, new ClassReference(importQualifiedName, classReference.getPackageName(), classReference.getClassName(),
                                        null, classReference.getResolutionStatus(), TypeReferenceLocation.IMPORT,
                                        classReference.getLineNumber(), classReference.getColumn(), classReference.getLength(), classReference.getLine()));
                        }

                        for (ClassReference reference : deduplicatedReferences.values())
                        {
                            if (TypeInterestFactory.matchesAny(reference.getQualifiedName(), reference.getLocation()))
                            {
                                foundMatch = true;
                                break;
                            }
                        }

                        if (foundMatch)
                            break;
                    }

                    // This is just to make it more readable. Mark them as skipped if there were no matches found.
                    boolean shouldSkip = !foundMatch;
                    for (JavaClassFileModel fileModel : classBatch)
                    {
                        fileModel.setSkipDecompilation(shouldSkip);
                    }
                }
                finally
                {
                    progressEstimate.addWork(classBatch.size());

                    if (progressEstimate.getWorked() % 1000 == 0)
                    {
                        long remainingTimeMillis = progressEstimate.getTimeRemainingInMillis();
                        if (remainingTimeMillis > 1000)
                            event.ruleEvaluationProgress(ClassFilePreDecompilationScan.class.getSimpleName(), progressEstimate.getWorked(), totalWork, (int) remainingTimeMillis / 1000);
                        LOG.info(progressEstimate.getWorked() + " / " + totalWork);
                    }
                }
            }

        }
        finally
        {
            ExecutionStatistics.get().end("ClassFilePreDecompilationScan.perform()");
        }
    }

    /**
     * This method is called on every reference that is in the .class file.
     *
     * @param typeReference
     * @return
     */
    private boolean shouldIgnore(String typeReference)
    {
        typeReference = typeReference.replace('/', '.').replace('\\', '.');
        return JavaClassIgnoreResolver.singletonInstance().matches(typeReference);
    }

    @Override
    public String toString()
    {
        return ClassFilePreDecompilationScan.class.getSimpleName();
    }

    private class ClassFileMap
    {
        /*
         * This maps from outer class filepaths, without inner classes) to a list of {@link JavaClassFileModel}s that includes any inner classes.
         *
         * For example, take the following files as an example: - /path/to/MyClass.class - /path/to/MyClass$1.class - /path/to/MyClass$1$1.class
         *
         * In this case, this would create a map that looks like this: - Key: /path/to/MyClass - Values: - /path/to/MyClass.class -
         * /path/to/MyClass$1.class - /path/to/MyClass$1$1.class
         */
        Map<String, List<JavaClassFileModel>> basePathToClassFiles = new HashMap<>();

        public Collection<List<JavaClassFileModel>> getClasses()
        {
            return basePathToClassFiles.values();
        }

        public void addClass(JavaClassFileModel javaClassFileModel)
        {
            String baseFilename = getBaseClassPath(javaClassFileModel);
            List<JavaClassFileModel> classFileList = basePathToClassFiles.get(baseFilename);
            if (classFileList == null)
            {
                classFileList = new ArrayList<>();
                basePathToClassFiles.put(baseFilename, classFileList);
            }
            classFileList.add(javaClassFileModel);
        }

        private String getBaseClassPath(JavaClassFileModel javaClassFileModel)
        {
            String filePath = javaClassFileModel.getFilePath();
            String filename = javaClassFileModel.getFileName();

            // This is not an inner class, so just return it without the ".class" at the end
            if (!StringUtils.contains(filename, "$"))
                return filePath.substring(0, filePath.length() - 6);

            String baseFilename = filePath.substring(0, filePath.indexOf("$"));

            /*
             * It looks a bit strange to reconstitute the full path like this instead of just searching filename itself, but this deals cleanly with
             * the edge case where a "$" is in the directory instead of the filename.
             */
            return Paths.get(filePath).getParent().resolve(baseFilename).toString();
        }
    }
}
