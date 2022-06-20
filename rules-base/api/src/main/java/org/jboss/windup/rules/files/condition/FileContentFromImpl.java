package org.jboss.windup.rules.files.condition;

/**
 * An implementation of FileContent created in order to support matches() as both, static and normal method.
 */
public class FileContentFromImpl implements FileContentFrom {
    private final String from;

    public FileContentFromImpl(String from) {
        this.from = from;
    }

    @Override
    public FileContentMatches matches(String matches) {
        FileContent f = new FileContent(matches);
        f.setInputVariablesName(from);
        return f;
    }

    @Override
    public FileContentFileName withFilesNamed(String filenamePattern) {
        FileContent f = new FileContent();
        f.setInputVariablesName(from);
        return f.inFileNamed(filenamePattern);
    }

    @Override
    public FileContent as(String as) {
        return null;
    }
}
