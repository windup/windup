package org.jboss.windup.rules.apps.java.xml;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.condition.JavaClassBuilderAt;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * 
 * Represents a {@link JavaClass} {@link Condition}.
 * 
 * Example:
 * 
 * <pre>
 * &lt;javaclass type="javax.servlet.http.HttpServletRequest"&gt;
 *         &lt;location&gt;METHOD_PARAMETER&lt;/location&gt;
 * &lt;/javaclass&gt;
 * </pre>
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
@NamespaceElementHandler(elementName = "javaclass", namespace = "http://windup.jboss.org/v1/xml")
public class JavaClassHandler implements ElementHandler<JavaClassBuilderAt>
{

    @Override
    public JavaClassBuilderAt processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String referencesRegex = $(element).attr("references");
        String typeNamePattern = $(element).attr("in");

        if (StringUtils.isBlank(referencesRegex) && StringUtils.isBlank(typeNamePattern))
        {
            throw new WindupException(
                        "Error, 'javaclass' element is lacking both 'references' and 'in' attributes (at least one of these is required)");
        }

        List<TypeReferenceLocation> locations = new ArrayList<TypeReferenceLocation>();
        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            TypeReferenceLocation location = handlerManager.processElement(child);
            locations.add(location);
        }

        JavaClass javaClass = new JavaClass();
        if (!StringUtils.isBlank(referencesRegex))
            javaClass.setRegex(referencesRegex);

        if (!StringUtils.isBlank(typeNamePattern))
        {
            javaClass.setTypeFilterRegex(typeNamePattern);
        }

        return javaClass.at(locations.toArray(new TypeReferenceLocation[locations.size()]));
    }
}
