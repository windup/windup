package org.jboss.windup.reporting.quickfix;

import java.io.File;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class QuickfixLocationDTO {
    private File reportDirectory;
    private File file;
    private int line;
    private int column;
    private int length;

    public QuickfixLocationDTO() {
    }

    public QuickfixLocationDTO(File reportDirectory, File file, int line, int column, int length) {
        this.reportDirectory = reportDirectory;
        this.file = file;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    public File getReportDirectory() {
        return reportDirectory;
    }

    public void setReportDirectory(File reportDirectory) {
        this.reportDirectory = reportDirectory;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
