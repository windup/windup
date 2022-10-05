package org.jboss.windup.bootstrap.commands.windup;

import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class InputsHandler {

    private static final Logger LOG = Logger.getLogger(InputsHandler.class.getName());

    /**
     * Handles input path(s). Behaviour will differ depending on the number on inputs: if a single input is given, and
     * it contains multiple archives, each will be considered to be a single application. If multiple inputs are given,
     * each will be considered to be an application.
     */
    public List<Path> handle(List<Path> inputs) {
        if (inputs.size() == 0) {
            throw new WindupException("No inputs found. At least one input must be specified.");
        } else if (inputs.size() == 1) {
            Path path = inputs.get(0);
            if (containsArchives(path)) {
                List<Path> archives = new LinkedList<>();
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                    for (Path subpath : directoryStream) {
                        if (isJavaArchive(subpath)) {
                            archives.add(subpath);
                        }
                    }
                    return archives;
                } catch (IOException e) {
                    throw new WindupException("Failed to read directory contents of: " + path + ": " + e.getMessage());
                }
            } else {
                return inputs;
            }
        } else {
            return inputs;
        }
    }

    private boolean containsArchives(Path path) {
        if (isJavaArchive(path))
            return false;

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            for (Path subpath : directoryStream) {
                if (isJavaArchive(subpath)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new WindupException("Failed to read directory contents of: " + path + ": " + e.getMessage());
        }
        return false;
    }

    private static boolean isJavaArchive(Path path) {
        return ZipUtil.endsWithZipExtension(path.toString());
    }
}
