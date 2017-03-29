package org.jboss.windup.tooling.data;

/**
 * QuickfixImpl object parsed from XML rule definition
 * 
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 *
 */
public class QuickfixImpl implements Quickfix
{
    private static final long serialVersionUID = 1L;

    private QuickfixType type;

    private String search;

    private String replacement;

    private String newline;

    private String name;
    
    /**
     * @return the name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the type
     */
    @Override
    public QuickfixType getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    @Override
    public void setType(QuickfixType type)
    {
        this.type = type;
    }

    /**
     * @return the search
     */
    @Override
    public String getSearch()
    {
        return search;
    }

    /**
     * @param search the search to set
     */
    @Override
    public void setSearch(String search)
    {
        this.search = search;
    }

    /**
     * @return the replacement
     */
    @Override
    public String getReplacement()
    {
        return replacement;
    }

    /**
     * @param replacement the replacement to set
     */
    @Override
    public void setReplacement(String replacement)
    {
        this.replacement = replacement;
    }

    /**
     * @return the newline
     */
    @Override
    public String getNewline()
    {
        return newline;
    }

    /**
     * @param newline the newline to set
     */
    @Override
    public void setNewline(String newline)
    {
        this.newline = newline;
    }
}
