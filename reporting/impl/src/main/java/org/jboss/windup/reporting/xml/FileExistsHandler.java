package org.jboss.windup.reporting.xml;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.FileExists;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Creates a {@link FileExists} that searches for the given file with filename. Example usage:
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *             &lt;not&gt;
 *                 &lt;file-exists filename="something.java" /&gt;
 *             &lt;/not&gt;
 *         &lt;/when&gt;
 *         &lt;perform&gt;
 *             [...]
 *         &lt;/perform&gt;
 *     &lt;/rule&gt;
 * </pre>
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
@NamespaceElementHandler(elementName = FileExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class FileExistsHandler implements ElementHandler<FileExists>
{
    static final String ELEMENT_NAME = "file-exists";
    private static final String FILENAME = "filename";

    @Override
    public FileExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String fileName = $(element).attr(FILENAME);

        if (StringUtils.isBlank(fileName))
        {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + FILENAME + "' attribute");
        }

        return FileExists.withFileName(fileName);
    }
}