package org.jboss.windup.rules.files.condition;

import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * Helping class for building {@link File} condition
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class FileFrom {
    private final File f = new File();

    public void setFrom(String from) {
        f.setInputVariablesName(from);
    }

    /**
     * Match filenames against the provided parameterized string.
     */
    public File inFileNamed(String filenamePattern) {
        if (filenamePattern != null && !filenamePattern.isEmpty()) {
            f.setFilenamePattern(new RegexParameterizedPatternParser(filenamePattern));
        }
        return f;
    }
}
