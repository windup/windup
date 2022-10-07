package org.jboss.windup.tooling.data;

import java.io.File;

/**
 * QuickfixImpl object parsed from XML rule definition
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public class QuickfixImpl implements Quickfix {
    private static final long serialVersionUID = 1L;

    private QuickfixType type;

    private String search;

    private String replacement;

    private String newline;

    private String name;

    private String transformationID;

    private File file;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public QuickfixType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(QuickfixType type) {
        this.type = type;
    }

    /**
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * @param search the search to set
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * @return the replacement
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * @param replacement the replacement to set
     */
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * @return the newline
     */
    public String getNewline() {
        return newline;
    }

    /**
     * @param newline the newline to set
     */
    public void setNewline(String newline) {
        this.newline = newline;
    }

    public String getTransformationID() {
        return transformationID;
    }

    public void setTransformationID(String transformationID) {
        this.transformationID = transformationID;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
