package org.jboss.windup.ast.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.ast.java.data.ClassReference;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ASTProcessor {
    /**
     * Processes a java file using the default {@link WildcardImportResolver}.
     *
     * See also: {@see JavaASTProcessor#analyze(WildcardImportResolver, Set, Set, Path)}
     */
    public static List<ClassReference> analyze(Set<String> libraryPaths, Set<String> sourcePaths, Path sourceFile)
    {
        return analyze(new NoopWildcardImportResolver(), libraryPaths, sourcePaths, sourceFile);
    }

    /**
     * Parses the provided file, using the given libraryPaths and sourcePaths as context. The libraries may be either jar files or references to
     * directories containing class files.
     *
     * The sourcePaths must be a reference to the top level directory for sources (eg, for a file src/main/java/org/example/Foo.java, the source path
     * would be src/main/java).
     *
     * The wildcard resolver provides a fallback for processing wildcard imports that the underlying parser was unable to resolve.
     */
    public static List<ClassReference> analyze(WildcardImportResolver importResolver, Set<String> libraryPaths, Set<String> sourcePaths,
                                               Path sourceFile)
    {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setEnvironment(libraryPaths.toArray(new String[libraryPaths.size()]), sourcePaths.toArray(new String[sourcePaths.size()]), null, true);
        parser.setBindingsRecovery(false);
        parser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);
        String fileName = sourceFile.getFileName().toString();
        parser.setUnitName(fileName);
        try
        {
            parser.setSource(FileUtils.readFileToString(sourceFile.toFile()).toCharArray());
        }
        catch (IOException e)
        {
            throw new ASTException("Failed to get source for file: " + sourceFile.toString() + " due to: " + e.getMessage(), e);
        }
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        return new ASTReferenceResolver(importResolver).analyze(sourceFile.toString(), cu);
    }
}
