package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.bootstrap.commands.AbstractListCommand;
import org.jboss.windup.bootstrap.commands.Command;
import org.jboss.windup.bootstrap.commands.CommandPhase;
import org.jboss.windup.bootstrap.commands.CommandResult;
import org.jboss.windup.bootstrap.commands.FurnaceDependent;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMappingRegistry;
import org.jboss.windup.util.ClassNameUtil;
import org.jboss.windup.util.PackageComparator;
import org.jboss.windup.util.PackageFrequencyTrie;
import org.jboss.windup.util.PackageFrequencyTrieVisitor;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.ZipUtil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoverPackagesCommand extends AbstractListCommand implements Command, FurnaceDependent {
    private final List<String> arguments;

    public DiscoverPackagesCommand(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Recursively scan the provided path and return a list of all Java packages contained therein.
     */

    private static Map<String, Integer> findClasses(Path path) {
        List<String> paths = findPaths(path, true);
        Map<String, Integer> results = new HashMap<>();
        for (String subPath : paths) {
            if (subPath.endsWith(".java") || subPath.endsWith(".class")) {
                String qualifiedName = PathUtil.classFilePathToClassname(subPath);
                addClassToMap(results, qualifiedName);
            }
        }
        return results;
    }

    private static void addClassToMap(Map<String, Integer> map, String className) {
        Integer count = map.get(className);
        if (count == null)
            map.put(className, 1);
        else
            map.put(className, count + 1);
    }

    /**
     * Find all paths within the given file (or folder).
     */
    private static Collection<String> findPaths(Path path) {
        List<String> paths = findPaths(path, false);
        Collections.sort(paths);
        return paths;
    }

    private static List<String> findPaths(Path path, boolean relativeOnly) {
        List<String> results = new ArrayList<>();
        results.add(path.normalize().toAbsolutePath().toString());
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path child : directoryStream) {
                    results.addAll(findPaths(child, relativeOnly));
                }
            } catch (IOException e) {
                System.err.println("Could not read file: " + path + " due to: " + e.getMessage());
            }
        } else if (Files.isRegularFile(path) && ZipUtil.endsWithZipExtension(path.toString())) {
            results.addAll(ZipUtil.scanZipFile(path, relativeOnly));
        }
        return results;
    }

    @Override
    public CommandResult execute() {
        String input = null;
        for (int i = 0; i < this.arguments.size(); i++) {
            String argument = this.arguments.get(i);
            if (argument.equalsIgnoreCase("--" + InputPathOption.NAME)) {
                if (this.arguments.size() > (i + 1)) {
                    input = this.arguments.get(i + 1);
                    break;
                }
            }
        }

        if (input == null) {
            System.err.println();
            System.err.println("ERROR: --input must be specified");
            return CommandResult.EXIT;
        }

        Path inputPath = Paths.get(input);
        if (!Files.isDirectory(inputPath) && !Files.isRegularFile(inputPath)) {
            System.err.println();
            System.err.println("ERROR: --input must exist");
            return CommandResult.EXIT;
        }

        if (Files.isRegularFile(inputPath) && !Files.isReadable(inputPath)) {
            System.err.println();
            System.err.println("ERROR: --input must be readable");
            return CommandResult.EXIT;
        }

        final Map<String, Integer> classes = findClasses(Paths.get(input));
        PackageNameMappingRegistry packageNameMappingRegistry = getFurnace().getAddonRegistry().getServices(PackageNameMappingRegistry.class).get();
        packageNameMappingRegistry.loadPackageMappings();

        Map<String, String> packageToOrganization = new TreeMap<>(new PackageComparator());
        PackageFrequencyTrie frequencyTrie = new PackageFrequencyTrie();

        for (String qualifiedName : classes.keySet()) {
            String packageName = ClassNameUtil.getPackageName(qualifiedName);
            String organization = packageNameMappingRegistry.getOrganizationForPackage(packageName);
            if (organization == null)
                frequencyTrie.addClass(qualifiedName);
            else
                packageToOrganization.put(packageName, organization);
        }

        System.out.println("Known Packages:");
        System.out.println("=======================");
        System.out.println();
        for (Map.Entry<String, String> organizationPackage : packageToOrganization.entrySet()) {
            System.out.println(organizationPackage.getKey() + " - " + organizationPackage.getValue());
        }

        System.out.println();
        System.out.println("Unknown Packages:");
        System.out.println("=======================");
        frequencyTrie.visit(new PackageFrequencyTrieVisitor() {

            @Override
            public void visit(PackageFrequencyTrie trie, int depth) {
                String packageName = trie.getPackageName();
                int recursiveClassCount = trie.getClassCount(true);
                if (depth == 1 || (depth > 1 && recursiveClassCount > 100))
                    System.out.println(packageName + " - Classes: " + recursiveClassCount);

                if (depth == 0 && trie.getClassCount(false) > 0) {
                    System.out.println("Default Package - Classes: " + trie.getClassCount(false));
                }
            }
        });

        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase() {
        return CommandPhase.PRE_EXECUTION;
    }

}
