package org.jboss.windup.rules.apps.xml.condition;

import org.junit.Assert;
import org.junit.Test;

public class XmlFileXPathTransformerTest
{

    @Test
    public void testXPathConversionSimple()
    {
        String result = XmlFileXPathTransformer.transformXPath("/foo[windup:matches(el, '{foo}')]");
        Assert.assertEquals("/foo[windup:startFrame(0) and windup:evaluate(0, windup:matches(0, el, '{foo}'))]/self::node()[windup:persist(0, .)]",
                    result);
    }

    @Test
    public void testXPathConversionComplexMultipleConditions()
    {
        String result = XmlFileXPathTransformer.transformXPath("/foo[el1 = 1234 and windup:matches(el, '{foo}')]/baz/tomato[1 = 1]");
        Assert.assertEquals(
                    "/foo[windup:startFrame(0) and windup:evaluate(0, el1 = 1234 and windup:matches(0, el, '{foo}'))]/baz/tomato[windup:startFrame(1) and windup:evaluate(1, 1 = 1)]/self::node()[windup:persist(1, .)]",
                    result);
    }
}
