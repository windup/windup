package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
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
@NamespaceElementHandler(elementName = PackageNameMappingHandler.ELEM_NAME, namespace = "http://windup.jboss.org/v1/xml")
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

        context.getBuilder().addRule(PackageNameMapping.fromPackage(from).toOrganization(to));
        return null;
    }

}
