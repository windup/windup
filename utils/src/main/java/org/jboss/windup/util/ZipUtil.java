package org.jboss.windup.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.util.Streams;
import org.jboss.windup.util.exception.WindupException;


/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class ZipUtil {
    private static final Logger log = Logger.getLogger(ZipUtil.class.getName());

    private static Set<String> supportedExtensions;

    private static final CopyOption[] COPY_OPTIONS = {
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES,
            LinkOption.NOFOLLOW_LINKS
    };

    /**
     * Unzip a classpath resource using the given {@link Class} as the resource root path.
     */
    public static void unzipFromClassResource(Class<?> clazz, String resourcePath, File extractToPath) throws IOException {
        File inputFile = File.createTempFile("windup-resource-to-unzip-", ".zip");
        try {
            try (final InputStream stream = clazz.getResourceAsStream(resourcePath)) {
                FileUtils.copyInputStreamToFile(stream, inputFile);
            }
            extractToPath.mkdirs();
            ZipUtil.unzipToFolder(inputFile, extractToPath);
        } finally {
            inputFile.delete();
        }
    }

    /**
     * Unzip the given {@link File} to the specified directory.
     */
    public static void unzipToFolder(File inputFile, File outputDir) throws IOException {
        if (inputFile == null)
            throw new IllegalArgumentException("Argument inputFile is null.");
        if (outputDir == null)
            throw new IllegalArgumentException("Argument outputDir is null.");

        try (ZipFile zipFile = new ZipFile(inputFile)) {
            Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
            while (entryEnum.hasMoreElements()) {
                ZipEntry entry = entryEnum.nextElement();
                String entryName = entry.getName();
                File destFile = new File(outputDir, entryName);
                if (!entry.isDirectory()) {
                    File parentDir = destFile.getParentFile();
                    if (!parentDir.isDirectory() && !parentDir.mkdirs()) {
                        throw new WindupException("Unable to create directory: " + parentDir.getAbsolutePath());
                    }
                    try (InputStream zipInputStream = zipFile.getInputStream(entry)) {
                        try (FileOutputStream outputStream = new FileOutputStream(destFile)) {
                            Streams.write(zipInputStream, outputStream);
                        }
                    }
                }
            }
        }
    }

    public static String getEndsWithZipRegularExpression() {
        Set<String> zipExtensions = getZipExtensions();
        final String regex;
        if (zipExtensions.size() == 1) {
            regex = ".+\\." + zipExtensions.iterator().next() + "$";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b(");
            for (String value : zipExtensions) {
                builder.append("|");
                builder.append(value);
            }
            builder.append(")\\b");
            regex = ".+\\." + builder.toString() + "$";
        }
        return regex;
    }

    public static boolean endsWithZipExtension(String path) {
        for (String extension : getZipExtensions()) {
            if (StringUtils.endsWith(path, "." + extension)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> getZipExtensions() {
        if (supportedExtensions == null) {
            Set<String> extensions = new HashSet<>();
            extensions.add("war");
            extensions.add("ear");
            extensions.add("jar");
            extensions.add("sar");
            extensions.add("rar");
            extensions.add("zip");
            supportedExtensions = extensions;
        }

        return supportedExtensions;
    }


    public static List<String> scanZipFile(Path zipFilePath, boolean relativeOnly) {
        try {
            try (final InputStream is = new FileInputStream(zipFilePath.toFile())) {
                return scanZipFile(zipFilePath.normalize().toString(), is, relativeOnly);
            }
        } catch (IOException e) {
            System.err.println("Could not read file: " + zipFilePath + " due to: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    public static List<String> scanZipFile(String parentPath, InputStream is, boolean relativeOnly) {
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            List<String> results = new ArrayList<>();
            while ((entry = getNextEntry(zis)) != null) {
                String fullPath = parentPath + "/" + entry.getName();
                results.add(relativeOnly ? entry.getName() : fullPath);
                if (!entry.isDirectory() && ZipUtil.endsWithZipExtension(entry.getName())) {
                    results.addAll(scanZipFile(fullPath, zis, relativeOnly));
                }
            }
            return results;
        } catch (IOException e) {
            System.err.println("Could not read file: " + parentPath + " due to: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Return the next Entry of a ZipInputStream. If the nextEntry can not be read then skip it and continue with the next one
     * @param zis The ZIP stream to be used
     * @return next zip entry
     * @throws IOException
     */
    private static ZipEntry getNextEntry(ZipInputStream zis) throws IOException {
        try {
            return zis.getNextEntry();
        } catch (IllegalArgumentException e) {
            return getNextEntry(zis);
        }
    }

    public static void zipFolder(Path source, String zipOutputPath, String zipOutputName, List<String> pathPrefixesToExclude) throws IOException {
        File outputFile = new File(zipOutputPath + File.separator + zipOutputName);
        URI outputFileURI = outputFile.toURI();

        final URI uri = URI.create("jar:" + outputFileURI);

        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                final Path targetFile = source.relativize(file);
                if (attributes.isSymbolicLink() || file.endsWith(zipOutputName) || pathPrefixesToExclude.stream().anyMatch(targetFile::startsWith)) {
                    return FileVisitResult.CONTINUE;
                }

                final Map<String, String> env = new HashMap<>();
                env.put("create", "true");

                try (final FileSystem zip = FileSystems.newFileSystem(uri, env)) {
                    final Path pathInZipfile = zip.getPath(targetFile.toString());
                    if (pathInZipfile.getParent() != null) {
                        Files.createDirectories(pathInZipfile.getParent());
                    }
                    Files.copy(file, pathInZipfile, COPY_OPTIONS);
                } catch (IOException e) {
                    throw new WindupException(e);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                final String message = String.format("Unable to zip : %s%n%s%n", file, exc);
                log.warning(message);
                System.err.println(message);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
