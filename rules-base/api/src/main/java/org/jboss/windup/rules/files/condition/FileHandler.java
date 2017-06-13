package org.jboss.windup.rules.files.condition;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

import java.util.logging.Logger;

import static org.joox.JOOX.$;

/**
 * Represents a {@link File} {@link Condition}..
 * <p/>
 * Example:
 * <p/>
 * <pre>
 * &lt;file  filename="{filename}" /&gt;
 * </pre>
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@NamespaceElementHandler(elementName = FileHandler.ELEM_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class FileHandler implements ElementHandler<File>
{
    public static final String ELEM_NAME = "file";
    private static final String ATTR_FILENAME = "filename";
    private static final String ATTR_FROM = "from";
    private static final Logger LOG = Logging.get(FileHandler.class);

    @Override
    public File processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String filenamePattern = $(element).attr(ATTR_FILENAME);
        String from = $(element).attr(ATTR_FROM);
        String as = $(element).attr("as");

        if (StringUtils.isBlank(filenamePattern))
        {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + ATTR_FILENAME + "' attribute");
        }

        Object obj = null;
        obj = applyFrom(obj, from);
        obj = applyFileName(obj, filenamePattern);
        //At least filename should have matched (otherwise exception would be thrown),
        // so FileContent instance should have been created
        File f = (File) obj;
        if(as!=null)
        {
            f.as(as);
        }
        return f;
    }

    private Object applyFrom(Object fileContentBuilder, String from)
    {
        if (from != null)
        {
            fileContentBuilder = File.from(from);
        }
        return fileContentBuilder;
    }

    private Object applyFileName(Object fileContentBuilder, String fileName)
    {
        if (fileName != null)
        {
            if (fileContentBuilder == null)
            {
                fileContentBuilder = File.inFileNamed(fileName);
            }
            else
            {
                fileContentBuilder = ((File) fileContentBuilder).inFileNamed(fileName);
            }
        }
        return fileContentBuilder;
    }
}
