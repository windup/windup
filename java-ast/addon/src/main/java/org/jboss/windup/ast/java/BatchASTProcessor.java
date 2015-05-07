package org.jboss.windup.ast.java;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

/**
 * Processes multiple files at a time in order to improve performance.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class BatchASTProcessor
{
    private static final int BATCH_SIZE = 1000 / Runtime.getRuntime().availableProcessors();
    private static final int THREADPOOL_SIZE = (Runtime.getRuntime().availableProcessors() / 2) + 1;

    /**
     * Process the given batch of files and pass the results back to the listener as each file is processed.
     */
    public static void analyze(final BatchASTListener listener, final WildcardImportResolver importResolver, final Set<String> libraryPaths,
                final Set<String> sourcePaths, Set<Path> sourceFiles)
    {
        ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);

        final String[] encodings = null;
        final String[] bindingKeys = new String[0];

        final FileASTRequestor requestor = new FileASTRequestor()
        {
            @Override
            public void acceptAST(String sourcePath, CompilationUnit ast)
            {
                try
                {
                    /**
                     * This super() call doesn't do anything, but we call it just to be nice, in case that ever changes.
                     */
                    super.acceptAST(sourcePath, ast);
                    ReferenceResolvingVisitor visitor = new ReferenceResolvingVisitor(importResolver, ast, sourcePath);
                    ast.accept(visitor);
                    listener.processed(Paths.get(sourcePath), visitor.getJavaClassReferences());
                }
                catch (Throwable t)
                {
                    listener.failed(Paths.get(sourcePath), t);
                }
            }
        };

        List<List<String>> batches = createBatches(sourceFiles);

        for (final List<String> batch : batches)
        {
            executor.submit(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    ASTParser parser = ASTParser.newParser(AST.JLS8);
                    parser.setBindingsRecovery(false);
                    parser.setResolveBindings(true);
                    Map<?, ?> options = JavaCore.getOptions();
                    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
                    parser.setCompilerOptions(options);
                    parser.setEnvironment(libraryPaths.toArray(new String[libraryPaths.size()]),
                                sourcePaths.toArray(new String[sourcePaths.size()]),
                                null,
                                true);

                    parser.createASTs(batch.toArray(new String[batch.size()]), encodings, bindingKeys, requestor, null);
                    return null;
                }
            });
        }

        try
        {
            executor.shutdown();
            while (executor.awaitTermination(10, TimeUnit.SECONDS) == false)
            {
                /*
                 * Shut down, then wait for termination in order to wait for all tasks to finish.
                 */
            }
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Interrupted while parsing Java sources.", e);
        }
    }

    private static List<List<String>> createBatches(Set<Path> sourceFiles)
    {
        List<List<String>> result = new ArrayList<>();
        List<String> batch = new ArrayList<>(BATCH_SIZE);
        for (Path path : sourceFiles)
        {
            if (batch.size() == BATCH_SIZE)
            {
                result.add(batch);
                batch = new ArrayList<>(BATCH_SIZE);
            }

            batch.add(path.toAbsolutePath().toString());
        }
        return result;
    }
}
