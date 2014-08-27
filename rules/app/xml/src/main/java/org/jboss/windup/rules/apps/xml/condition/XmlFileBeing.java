package org.jboss.windup.rules.apps.xml.condition;

import org.jboss.windup.reporting.config.Classification;

/**
 * Intermediate step for constructing {@link XmlFile} instances for a specified ref.
 * 
 * @author mbriskar
 */
public class XmlFileBeing
{
    private XmlFile xmlFile;

    XmlFileBeing(String variable)
    {
        this.xmlFile = new XmlFile();
    }

    /**
     * Set the xpath of this {@link XmlFile}. 
     */
    public XmlFile matchesXpath(String xpath)
    {
        this.xmlFile.setXpath(xpath);
        return this.xmlFile;
    }
    
    public XmlFile withDTDPublicId(String regex)
    {
        this.xmlFile.setPublicId(regex);
        return this.xmlFile;
    }
    
    public XmlFile resultMatches(String regex)
    {
        this.xmlFile.setXpathResultMatch(regex);
        return this.xmlFile;
    }
}
