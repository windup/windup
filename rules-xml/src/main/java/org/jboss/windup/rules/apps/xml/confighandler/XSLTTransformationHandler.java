package org.jboss.windup.rules.apps.xml.confighandler;

import static org.joox.JOOX.$;

import java.nio.file.Path;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
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
        String xsltFile = $(element).attr("xsltFile");

        Path pathContainingXml = handlerManager.getXmlInputPath();
        if (pathContainingXml != null)
        {
            String fullPath;
            if (xsltFile.startsWith("/") || xsltFile.startsWith("\\"))
            {
                fullPath = xsltFile;
            }
            else
            {
                fullPath = pathContainingXml.resolve(xsltFile).toAbsolutePath().toString();
            }
            return XSLTTransformation
                        .usingFilesystem(fullPath)
                        .withDescription(description)
                        .withExtension(extension);
        }
        else
        {
            ClassLoader xmlFileAddonClassLoader = handlerManager.getAddonContainingInputXML().getClassLoader();
            return XSLTTransformation
                        .using(xsltFile, xmlFileAddonClassLoader)
                        .withDescription(description)
                        .withExtension(extension);
        }
    }
}
