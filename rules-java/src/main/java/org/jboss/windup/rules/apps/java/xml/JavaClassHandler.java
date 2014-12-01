package org.jboss.windup.rules.apps.java.xml;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.condition.JavaClassBuilder;
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
        String type = $(element).attr("references");
        String as = $(element).attr("as");
        if(as == null) {
            as = Iteration.DEFAULT_VARIABLE_LIST_STRING;
        }
        if (StringUtils.isBlank(type))
        {
            throw new WindupException("Error, 'javaclass' element must have a non-empty 'type' attribute");
        }

        List<TypeReferenceLocation> locations = new ArrayList<TypeReferenceLocation>();
        List<Element> children = $(element).children("location").get();
        for (Element child : children)
        {
            TypeReferenceLocation location = handlerManager.processElement(child);
            locations.add(location);
        }

        JavaClassBuilder javaClassReferences = JavaClass.references(type);
        String namePattern = $(element).attr("in");
        if (!StringUtils.isBlank(namePattern))
        {
            javaClassReferences.inType(namePattern);
        }
        
        JavaClassBuilderAt javaClass = javaClassReferences.at(
                    locations.toArray(new TypeReferenceLocation[locations.size()]));
        javaClass.as(as);
        return javaClass;
    }
}
