/**
 *
 */
package org.jboss.windup.reporting.quickfix;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.QuickfixModel;
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.reporting.model.ReplacementQuickfixModel;
import org.jboss.windup.reporting.model.TransformationQuickfixModel;
import org.jboss.windup.reporting.service.QuickfixService;
import org.jboss.windup.util.exception.WindupException;

/**
 * This is a pojo for setting and getting Quickfix
 *
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 *
 */
public class Quickfix {
    private QuickfixType type;

    private String name;

    private String newline;

    private String replacementStr;

    private String searchStr;

    private String transformationID;

    private FileModel fileModel;

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

    /**
     * @return the replacementStr
     */
    public String getReplacementStr() {
        return replacementStr;
    }

    /**
     * @param replacementStr the replacementStr to set
     */
    public void setReplacementStr(String replacementStr) {
        this.replacementStr = replacementStr;
    }

    /**
     * @return the searchStr
     */
    public String getSearchStr() {
        return searchStr;
    }

    /**
     * @param searchStr the searchStr to set
     */
    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public FileModel getFileModel() {
        return fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    public String getTransformationID() {
        return transformationID;
    }

    public void setTransformationID(String transformationID) {
        this.transformationID = transformationID;
    }

    public QuickfixModel createQuickfix(GraphContext graphContext) {
        QuickfixService quickfixService = new QuickfixService(graphContext);
        QuickfixModel quickfixModel = quickfixService.create();
        quickfixModel.setQuickfixType(getType());
        quickfixModel.setName(StringUtils.trim(getName()));

        switch (getType()) {
            case INSERT_LINE:
            case DELETE_LINE:
            case REPLACE:
            case REGULAR_EXPRESSION:
                ReplacementQuickfixModel replacementQuickfixModel = GraphService.addTypeToModel(graphContext, quickfixModel, ReplacementQuickfixModel.class);
                replacementQuickfixModel.setReplacement(StringUtils.trim(getReplacementStr()));
                replacementQuickfixModel.setSearch(StringUtils.trim(getSearchStr()));
                replacementQuickfixModel.setNewline(StringUtils.trim(getNewline()));
                break;
            case TRANSFORMATION:
                TransformationQuickfixModel transformationQuickfixModel = GraphService.addTypeToModel(graphContext, quickfixModel, TransformationQuickfixModel.class);
                transformationQuickfixModel.setTransformationID(getTransformationID());
                transformationQuickfixModel.setFileModel(getFileModel());
                break;
            default:
                throw new WindupException("Unrecognized quickfix type: " + getType());
        }
        return quickfixModel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(type + " Name: " + getName());

        if (type == QuickfixType.INSERT_LINE) {
            sb.append(" newline: " + getNewline());
        }
        if (type == QuickfixType.REPLACE) {
            sb.append(" search: " + getSearchStr());
            sb.append(" replacement: " + getReplacementStr());
        }
        return sb.toString();
    }

}
