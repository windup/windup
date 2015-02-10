package org.jboss.windup.rules.files;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Handles parsing the "file-mapping" element to add rules to the current ruleset.
 */
@NamespaceElementHandler(elementName = FileMappingHandler.ELEM_NAME, namespace = "http://windup.jboss.org/v1/xml")
public class FileMappingHandler implements ElementHandler<Void>
{
    protected static final String ELEM_NAME = "file-mapping";
    private static final String FROM = "from";
    private static final String TO = "to";

    @Inject
    private GraphTypeManager typeManager;

    @Override
    @SuppressWarnings("unchecked")
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

        List<Class<? extends WindupVertexFrame>> types = new ArrayList<>();
        List<String> typeNames = Arrays.asList(to.trim().split("\\s*,\\s*"));
        for (String name : typeNames)
        {
            List<Class<? extends WindupVertexFrame>> matchingTypes = new ArrayList<>();
            for (Class<? extends WindupVertexFrame> modelType : typeManager.getRegisteredTypes())
            {
                if (modelType.getName().equals(name) || modelType.getSimpleName().equals(name + ".class") || modelType.getSimpleName().equals(name + "Model") || modelType.getSimpleName().equals(name))
                {
                    matchingTypes.add(modelType);
                }
            }
            if (matchingTypes.isEmpty())
            {
                throw new WindupException("A '" + ELEM_NAME + "' element specifies a file mapping type [" + name
                            + "] that could not be found in any installed addons.");
            }
            if (matchingTypes.size() > 1)
            {
                throw new WindupException("A '" + ELEM_NAME + "' element specifies a file mapping type ["
                            + name + "] that matched multiple file types. Please select one of matches and "
                            + "update the configuration to use the fully qualified name: ["
                            + matchingTypes.toString().replaceAll(",", "\n") + "]");
            }
            types.addAll(matchingTypes);
        }

        context.getBuilder().addRule(FileMapping.from(from).to(types.toArray(new Class[types.size()])));
        return null;
    }

}
