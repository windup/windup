package org.jboss.windup.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.util.exception.WindupException;

/**
 * Utility for visiting files matching a specific filename suffix pattern.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FileVisit
{
    /**
     * Visit all files with the given suffix pattern (regex) from the provided {@link File} directory tree.
     */
    public static void visit(File directory, Predicate<File> predicate, Visitor<File> visitor)
    {
        if (directory.exists())
        {
            List<File> files = visit(directory, predicate);
            for (File file : files)
            {
                visitor.visit(file);
            }
        }
    }

    private static List<File> visit(final File directory, final Predicate<File> predicate)
    {
        try
        {
            final List<File> result = new ArrayList<>();
            if (directory != null && directory.isDirectory())
            {
                Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                    {
                        if (predicate.accept(file.toFile()))
                            result.add(file.toFile());
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return result;
        }
        catch (Exception e)
        {
            throw new WindupException("Failed to visit directory filesystem at [" + directory + "]", e);
        }
    }

}
