package org.jboss.windup.bootstrap.commands.windup;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.jboss.windup.bootstrap.commands.*;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMappingRegistry;
import org.jboss.windup.util.*;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class DiscoverPackagesCommand extends AbstractListCommand implements Command, FurnaceDependent
{
    private final List<String> arguments;

    private Map<String, List<String>> knownPackages = new HashMap<>();
    private Map<String, Integer> unknownPackages = new HashMap<>();

    public DiscoverPackagesCommand(List<String> arguments)
    {
        this.arguments = arguments;
    }

    protected PackageNameMappingRegistry getPackageNameMappingRegistry()
    {
        return getFurnace().getAddonRegistry().getServices(PackageNameMappingRegistry.class).get();
    }

    @Override
    public CommandResult execute()
    {
        this.knownPackages.clear();
        this.unknownPackages.clear();

        String input = null;
        for (int i = 0; i < this.arguments.size(); i++)
        {
            String argument = this.arguments.get(i);
            if (argument.equalsIgnoreCase("--" + InputPathOption.NAME))
            {
                if (this.arguments.size() > (i + 1))
                {
                    input = this.arguments.get(i + 1);
                    break;
                }
            }
        }

        if (input == null)
        {
            System.err.println();
            System.err.println("ERROR: --input must be specified");
            return CommandResult.EXIT;
        }

        final Map<String, Integer> classes = findClasses(Paths.get(input), input);
        PackageNameMappingRegistry packageNameMappingRegistry = this.getPackageNameMappingRegistry();
        packageNameMappingRegistry.loadPackageMappings();

        Map<String, String> packageToOrganization = new TreeMap<>(new PackageComparator());
        PackageFrequencyTrie frequencyTrie = new PackageFrequencyTrie();

        for (String qualifiedName : classes.keySet())
        {
            String packageName = ClassNameUtil.getPackageName(qualifiedName);
            String organization = packageNameMappingRegistry.getOrganizationForPackage(packageName);
            if (organization == null)
            {
                frequencyTrie.addClass(qualifiedName);
            }
            else
            {
                packageToOrganization.put(packageName, organization);

                if (!this.knownPackages.containsKey(organization))
                {
                    this.knownPackages.put(organization, new ArrayList<>());
                }

                this.knownPackages.get(organization).add(packageName);
            }
        }

        System.out.println("Known Packages:");
        System.out.println("=======================");
        System.out.println();
        for (Map.Entry<String, String> organizationPackage : packageToOrganization.entrySet())
        {
            System.out.println(organizationPackage.getKey() + " - " + organizationPackage.getValue());
        }

        System.out.println();
        System.out.println("Unknown Packages:");
        System.out.println("=======================");
        frequencyTrie.visit(new PackageFrequencyTrieVisitor()
        {

            @Override
            public void visit(PackageFrequencyTrie trie, int depth)
            {
                String packageName = trie.getPackageName();
                int recursiveClassCount = trie.getClassCount(true);
                if (depth == 1 || (depth > 1 && recursiveClassCount > 100))
                {
                    System.out.println(packageName + " - Classes: " + recursiveClassCount);
                    unknownPackages.put(packageName, recursiveClassCount);
                }

                Integer nonRecursiveClassCount = trie.getClassCount(false);

                if (depth == 0 && nonRecursiveClassCount > 0)
                {
                    System.out.println("Default Package - Classes: " + nonRecursiveClassCount);
                    unknownPackages.put("", nonRecursiveClassCount);
                }
            }
        });

        return CommandResult.EXIT;
    }

    @Override
    public CommandPhase getPhase()
    {
        return CommandPhase.PRE_EXECUTION;
    }

    /**
     * Recursively scan the provided path and return a list of all Java packages contained therein.
     */

    private static Map<String, Integer> findClasses(Path path, String sourceRoot)
    {
        List<String> paths = findPaths(path, true);
        Map<String, Integer> results = new HashMap<>();
        for (String subPath : paths)
        {
            if (subPath.endsWith(".java") || subPath.endsWith(".class"))
            {
                String relativePath = subPath;

                if (subPath.contains(sourceRoot))
                {
                    relativePath = subPath.substring(sourceRoot.length());
                }

                String qualifiedName = PathUtil.classFilePathToClassname(relativePath);
                addClassToMap(results, qualifiedName);
            }
        }
        return results;
    }

    private static void addClassToMap(Map<String, Integer> map, String className)
    {
        Integer count = map.get(className);
        if (count == null)
            map.put(className, 1);
        else
            map.put(className, count + 1);
    }

    /**
     * Find all paths within the given file (or folder).
     */
    private static Collection<String> findPaths(Path path)
    {
        List<String> paths = findPaths(path, false);
        Collections.sort(paths);
        return paths;
    }

    private static List<String> findPaths(Path path, boolean relativeOnly)
    {
        List<String> results = new ArrayList<>();
        results.add(path.normalize().toAbsolutePath().toString());
        if (Files.isDirectory(path))
        {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path))
            {
                for (Path child : directoryStream)
                {
                    results.addAll(findPaths(child, relativeOnly));
                }
            }
            catch (IOException e)
            {
                System.err.println("Could not read file: " + path + " due to: " + e.getMessage());
            }
        }
        else if (Files.isRegularFile(path) && ZipUtil.endsWithZipExtension(path.toString()))
        {
            results.addAll(ZipUtil.scanZipFile(path, relativeOnly));
        }
        return results;
    }

    public Map<String, List<String>> getKnownPackages()
    {
        return knownPackages;
    }

    public Map<String, Integer> getUnknownPackages()
    {
        return unknownPackages;
    }
}
