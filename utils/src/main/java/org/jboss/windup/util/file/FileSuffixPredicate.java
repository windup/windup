package org.jboss.windup.util.file;

import java.io.File;
import java.util.regex.Pattern;

import org.jboss.forge.furnace.util.Predicate;

/**
 * Accepts a file based on its trailing filename.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class FileSuffixPredicate implements Predicate<File>
{
    private final String suffixPattern;

    /**
     * Create a new {@link FileSuffixPredicate} using the given regular expression to match the end of the filename.
     */
    public FileSuffixPredicate(String suffixPattern)
    {
        super();
        this.suffixPattern = suffixPattern;
    }

    public static FileSuffixPredicate fromLiteral(String literal)
    {
        return new FileSuffixPredicate(Pattern.quote(literal));
    }

    @Override
    public boolean accept(File file)
    {
        return file.toString().matches(".*" + suffixPattern + "$");
    }
}
