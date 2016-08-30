/**
 *
 */
package org.jboss.windup.reporting.config;

import org.jboss.windup.reporting.model.QuickfixType;

/**
 * This is a pojo for setting and getting Quickfix
 * 
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 *
 */
public class Quickfix
{
    private QuickfixType type;

    private String name;

    private String newline;

    private String replacementStr;

    private String searchStr;

    /**
     * @return the type
     */
    public QuickfixType getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(QuickfixType type)
    {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the newline
     */
    public String getNewline()
    {
        return newline;
    }

    /**
     * @param newline the newline to set
     */
    public void setNewline(String newline)
    {
        this.newline = newline;
    }

    /**
     * @return the replacementStr
     */
    public String getReplacementStr()
    {
        return replacementStr;
    }

    /**
     * @param replacementStr the replacementStr to set
     */
    public void setReplacementStr(String replacementStr)
    {
        this.replacementStr = replacementStr;
    }

    /**
     * @return the searchStr
     */
    public String getSearchStr()
    {
        return searchStr;
    }

    /**
     * @param searchStr the searchStr to set
     */
    public void setSearchStr(String searchStr)
    {
        this.searchStr = searchStr;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(type + " Name: " + getName());

        if (type == QuickfixType.INSERT_LINE)
        {
            sb.append(" newline: " + getNewline());
        }
        if (type == QuickfixType.REPLACE)
        {
            sb.append(" search: " + getSearchStr());
            sb.append(" replacement: " + getReplacementStr());
        }
        return sb.toString();
    }

}
