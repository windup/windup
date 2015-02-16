package org.jboss.windup.rules.xml;

import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.junit.Assert;
import org.junit.Test;

public class XmlFileTest
{

    /**
     * Testing that .from() and .as() sets the right variable
     */
    @Test
    public void xmlFileInputOutputVariableTest() {
        XmlFile as = (XmlFile)XmlFile.from("input").matchesXpath("abc").as("output");
        Assert.assertEquals("input", as.getInputVariablesName());
        Assert.assertEquals("output", as.getOutputVariablesName());
    }
}
