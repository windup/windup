package org.jboss.windup.rules.files.condition;

import static org.joox.JOOX.$;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Condition;
import org.w3c.dom.Element;

/**
 * Represents a {@link FileContent} {@link Condition}.
 * <p>
 * Example:
 *
 * <pre>
 * &lt;filecontent pattern="Some example {text}"&gt; filename="{filename}" /&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = FileContentHandler.ELEM_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class FileContentHandler implements ElementHandler<FileContent> {
    public static final String ELEM_NAME = "filecontent";
    private static final String ATTR_PATTERN = "pattern";
    private static final String ATTR_FILENAME = "filename";
    private static final String ATTR_FROM = "from";
    private static final Logger LOG = Logging.get(FileContentHandler.class);

    @Override
    public FileContent processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String contentPattern = $(element).attr(ATTR_PATTERN);
        String filenamePattern = $(element).attr(ATTR_FILENAME);
        String from = $(element).attr(ATTR_FROM);
        String as = $(element).attr("as");
        if (as == null) {
            as = Iteration.DEFAULT_VARIABLE_LIST_STRING;
        }

        if (StringUtils.isBlank(contentPattern)) {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + ATTR_PATTERN + "' attribute");
        }

        Object obj = null;
        obj = applyFrom(obj, from);
        obj = applyMatches(obj, contentPattern);
        obj = applyFileName(obj, filenamePattern);
        //At least content pattern should have matched (otherwise exception would be thrown),
        // so FileContent instance should have been created
        FileContent f = (FileContent) obj;
        f.as(as);
        if (StringUtils.isBlank(from) && StringUtils.isBlank(filenamePattern)) {
            LOG.warning("One of the filecontent conditions (" + f.toString() + ") is scanning all the files for a regex. This may have"
                    + "significant performance overhead.");
        }

        return f;
    }

    private Object applyFrom(Object fileContentBuilder, String from) {
        if (from != null) {
            fileContentBuilder = FileContent.from(from);
        }
        return fileContentBuilder;
    }

    private Object applyMatches(Object fileContentBuilder, String contentPattern) {
        if (contentPattern != null) {
            if (fileContentBuilder == null) {
                fileContentBuilder = FileContent.matches(contentPattern);
            } else {
                fileContentBuilder = ((FileContentFrom) fileContentBuilder).matches(contentPattern);
            }
        }
        return fileContentBuilder;
    }

    private Object applyFileName(Object fileContentBuilder, String fileName) {
        if (fileName != null) {
            if (fileContentBuilder == null) {
                //We do not support starting with fileName without content. Should use File condition. This should not happen thanks to checks
            } else {
                fileContentBuilder = ((FileContent) fileContentBuilder).inFileNamed(fileName);
            }
        }
        return fileContentBuilder;
    }
}
