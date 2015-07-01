package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.w3c.dom.Element;

/**
 * Handles parsing the "package-mapping" element to add rules to the current ruleset.<br/>
 * 
 * Example element:<br/>
 * 
 * <pre>
 * &lt;package-mapping from="org.apache" to="Apache"/&gt;
 * </pre>
 */
@NamespaceElementHandler(elementName = PackageNameMappingHandler.ELEM_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class PackageNameMappingHandler implements ElementHandler<Void>
{
    protected static final String ELEM_NAME = "package-mapping";
    private static final String FROM = "from";
    private static final String TO = "to";

    @Override
    public Void processElement(ParserContext context, Element element)
    {
        String from = $(element).attr(FROM);
        String to = $(element).attr(TO);
        if (StringUtils.isBlank(from))
        {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + FROM + "' attribute");
        }
        if (StringUtils.isBlank(to))
        {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + TO + "' attribute");
        }

        Rule rule = PackageNameMapping.fromPackage(from).toOrganization(to);
        if (rule instanceof Context)
            ((Context) rule).put(RuleMetadataType.RULE_XML, XmlUtil.nodeToString(element));
        context.getBuilder().addRule(rule);
        return null;
    }

}
