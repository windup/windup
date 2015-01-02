package org.jboss.windup.rules.apps.xml.confighandler;

import static org.joox.JOOX.$;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.NamespaceEntry;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

/**
 * Represents an {@link XSLTTransformation} {@link Condition}.
 * 
 * Example:
 * 
 * <pre>
 *  &lt;xslt description="weblogic.xml converted to jboss.xml" extension="-transformed-file.xml" xsltFile="path/to/xsltfile"/&gt;
 * </pre>
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
@NamespaceElementHandler(elementName = "xslt", namespace = "http://windup.jboss.org/v1/xml")
public class XSLTTransformationHandler implements ElementHandler<XSLTTransformation>
{

    @Override
    public XSLTTransformation processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String description = $(element).attr("description");
        String extension = $(element).attr("extension");
        String template = $(element).attr("template");
        String of = $(element).attr("of");

        if (StringUtils.isBlank(description))
        {
            throw new WindupException("Error, 'xslt' element must have a non-empty 'description' attribute");
        }
        if (StringUtils.isBlank(template))
        {
            throw new WindupException("Error, 'xslt' element must have a non-empty 'template' attribute");
        }
        if (StringUtils.isBlank(extension))
        {
            throw new WindupException("Error, 'xslt' element must have a non-empty 'extension' attribute");
        }
        Map<String, String> parameters = new HashMap<String, String>();
        List<Element> children = $(element).children("xslt-parameter").get();
        for (Element child : children)
        {
            XSLTParameter param = handlerManager.processElement(child);
            parameters.put(param.getKey(), param.getValue());
        }

        Path pathContainingXml = handlerManager.getXmlInputPath();
        if (pathContainingXml != null)
        {
            String fullPath;
            if (template.startsWith("/") || template.startsWith("\\"))
            {
                fullPath = template;
            }
            else
            {
                fullPath = pathContainingXml.resolve(template).toAbsolutePath().toString();
            }
            if (of != null)
            {
                return (XSLTTransformation)XSLTTransformation
                            .of(of)
                            .usingFilesystem(fullPath)
                            .withDescription(description)
                            .withExtension(extension)
                            .withParameters(parameters);
            }
            return (XSLTTransformation)XSLTTransformation
                        .usingFilesystem(fullPath)
                        .withDescription(description)
                        .withExtension(extension)
                        .withParameters(parameters);
        }
        else
        {
            ClassLoader xmlFileAddonClassLoader = handlerManager.getAddonContainingInputXML().getClassLoader();
            if (of != null)
            {
                return (XSLTTransformation)XSLTTransformation
                            .of(of)
                            .using(template, xmlFileAddonClassLoader)
                            .withDescription(description)
                            .withExtension(extension)
                            .withParameters(parameters);
            }
            return (XSLTTransformation) XSLTTransformation
                        .using(template, xmlFileAddonClassLoader)
                        .withDescription(description)
                        .withExtension(extension)
                        .withParameters(parameters);
        }
    }
}
