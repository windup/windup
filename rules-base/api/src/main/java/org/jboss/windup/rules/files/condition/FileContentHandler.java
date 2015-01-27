package org.jboss.windup.rules.files.condition;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

/**
 * Represents a {@link FileContent} {@link Condition}.
 * 
 * Example:
 *
 * <pre>
 * &lt;filecontent pattern="Some example {text}"&gt; filename="{filename}" /&gt;
 * </pre>
 * 
 * @author jsightler
 *
 */
@NamespaceElementHandler(elementName = FileContentHandler.ELEM_NAME, namespace = "http://windup.jboss.org/v1/xml")
public class FileContentHandler implements ElementHandler<FileContent>
{
    public static final String ELEM_NAME = "filecontent";
    private static final String ATTR_PATTERN = "pattern";
    private static final String ATTR_FILENAME = "filename";

    @Override
    public FileContent processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String contentPattern = $(element).attr(ATTR_PATTERN);
        String filenamePattern = $(element).attr(ATTR_FILENAME);
        String as = $(element).attr("as");
        if (as == null)
        {
            as = Iteration.DEFAULT_VARIABLE_LIST_STRING;
        }

        if (StringUtils.isBlank(contentPattern))
        {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + ATTR_PATTERN + "' attribute");
        }

        if (StringUtils.isBlank(filenamePattern))
        {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + ATTR_FILENAME + "' attribute");
        }

        FileContent fileContent = FileContent.matches(contentPattern).inFilesNamed(filenamePattern);
        fileContent.setOutputVariablesName(as);
        return fileContent;
    }
}
