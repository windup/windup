package org.jboss.windup.util.file;

import org.jboss.forge.furnace.util.Predicate;

import java.io.File;

/**
 * Accepts a file based on its trailing filename.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class FileSuffixPredicate implements Predicate<File> {
    private final String suffixPattern;

    /**
     * Create a new {@link FileSuffixPredicate} using the given regular expression to match the end of the filename.
     */
    public FileSuffixPredicate(String suffixPattern) {
        super();
        this.suffixPattern = suffixPattern;
    }

    @Override
    public boolean accept(File file) {
        return file.toString().matches(".*" + suffixPattern + "$");
    }
}
