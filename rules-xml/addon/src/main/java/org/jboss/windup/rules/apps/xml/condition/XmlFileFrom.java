package org.jboss.windup.rules.apps.xml.condition;

/**
 * Intermediate step for constructing {@link XmlFile} instances for a specified ref.
 * 
 */
public class XmlFileFrom
{
    private XmlFile xmlFile;

    XmlFileFrom(String variable)
    {
        this.xmlFile = new XmlFile();
        xmlFile.setInputVariablesName(variable);
    }

    /**
     * Set the xpath of this {@link XmlFile}.
     */
    public XmlFileXpath matchesXpath(String xpath)
    {
        this.xmlFile.setXpath(xpath);
        return this.xmlFile;
    }

    public XmlFileDTD withDTDPublicId(String regex)
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
