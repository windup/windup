package org.jboss.windup.config.parser.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractLabelProvider;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.*;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Handles parsing the "labelset" element
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
@NamespaceElementHandler(elementName = "labelset", namespace = LabelProviderHandler.WINDUP_LABEL_NAMESPACE)
public class LabelProviderHandler implements ElementHandler<LabelProvider>
{

    public static final String WINDUP_LABEL_NAMESPACE = "http://windup.jboss.org/schema/jboss-labelset";

    public static final String DESCRIPTION = "description";
    public static final String TRANSFORM = "labels";

    @Override
    public LabelProvider processElement(ParserContext context, Element element) throws ConfigurationException
    {
        String ID = $(element).attr("id");

        String priorityString = $(element).attr("priority");
        Integer priority = priorityString != null ? Integer.parseInt(priorityString) : null;

        String description = null;
        List<Label> labels = new ArrayList<>();

        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            if (StringUtils.equals(DESCRIPTION, child.getTagName())) {
                description = $(child).text();
            } else if (StringUtils.equals(DESCRIPTION, child.getTagName())) {
                description = $(child).text();
            } else if (StringUtils.equals(TRANSFORM, child.getTagName())) {
                List<Label> l = context.processElement(child);
                labels.addAll(l);
            }
        }

        List<Label> allLabels = labels;
        LabelProviderData data = new LabelProviderData() {
            @Override
            public List<Label> getLabels() {
                return allLabels;
            }
        };

        LabelProviderMetadata metadata;
        if (priority == null) {
            metadata = new LabelMetadataBuilder(ID, description);
        } else {
            metadata = new LabelMetadataBuilder(ID, priority, description);
        }
        return new AbstractLabelProvider(metadata, data);
    }

}
