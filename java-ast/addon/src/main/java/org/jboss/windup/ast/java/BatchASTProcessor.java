package org.jboss.windup.ast.java;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.jboss.windup.ast.java.data.ClassReference;

/**
 * Processes multiple files at a time in order to improve performance.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class BatchASTProcessor
{
    private static final int BATCH_SIZE = 1000;

    /**
     * Process the given batch of files and pass the results back to the listener as each file is processed.
     */
    public static void analyze(final BatchASTListener listener, WildcardImportResolver importResolver, Set<String> libraryPaths,
                Set<String> sourcePaths, Iterable<Path> sourceFiles)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS8);

        String[] encodings = null;
        String[] bindingKeys = new String[0];

        final ASTReferenceResolver referenceResolver = new ASTReferenceResolver(importResolver);

        FileASTRequestor requestor = new FileASTRequestor()
        {
            @Override
            public void acceptAST(String sourcePath, CompilationUnit ast)
            {
                try
                {
                    super.acceptAST(sourcePath, ast);
                    List<ClassReference> references = referenceResolver.analyze(sourcePath, ast);
                    listener.processed(Paths.get(sourcePath), references);
                }
                catch (Throwable t)
                {
                    listener.failed(Paths.get(sourcePath), t);
                }
            }
        };

        Iterator<Path> pathIterator = sourceFiles.iterator();
        List<String> batch = new ArrayList<>(BATCH_SIZE);
        while (pathIterator.hasNext())
        {
            batch.add(pathIterator.next().toAbsolutePath().toString());

            if (batch.size() == BATCH_SIZE || !pathIterator.hasNext())
            {
                parser.setEnvironment(libraryPaths.toArray(new String[libraryPaths.size()]), sourcePaths.toArray(new String[sourcePaths.size()]),
                            null, true);
                parser.setBindingsRecovery(false);
                parser.setResolveBindings(true);

                Map options = JavaCore.getOptions();
                JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);

                // these options seem to slightly reduce the number of times that JDT aborts on compilation errors
                options.put(JavaCore.CORE_INCOMPLETE_CLASSPATH, "warning");
                options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, "warning");
                options.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, "warning");
                options.put(JavaCore.CORE_CIRCULAR_CLASSPATH, "warning");
                options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, "warning");
                options.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, "warning");
                options.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, "ignore");
                options.put(JavaCore.COMPILER_PB_NULL_ANNOTATION_INFERENCE_CONFLICT, "warning");
                options.put(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, "warning");

                parser.setCompilerOptions(options);
                parser.createASTs(batch.toArray(new String[batch.size()]), encodings, bindingKeys, requestor, null);
                batch.clear();
            }
        }
    }
}
