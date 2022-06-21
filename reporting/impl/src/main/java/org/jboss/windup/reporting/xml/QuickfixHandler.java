/**
 *
 */
package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 *
 */
@NamespaceElementHandler(elementName = "quickfix", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class QuickfixHandler implements ElementHandler<Object> {

    @Override
    public Quickfix processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String name = $(element).attr("name");
        String quickfixTypeStr = $(element).attr("type");

        Quickfix quickfix = new Quickfix();
        quickfix.setName(name);
        quickfix.setType(QuickfixType.valueOf(quickfixTypeStr));

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            switch (child.getNodeName()) {
                case "newline":
                    quickfix.setNewline(StringUtils.trim(child.getFirstChild().getNodeValue()));
                    break;
                case "replacement":
                    quickfix.setReplacementStr(StringUtils.trim(child.getFirstChild().getNodeValue()));
                    break;
                case "search":
                    quickfix.setSearchStr(StringUtils.trim(child.getFirstChild().getNodeValue()));
                    break;
                case "implementationID":
                    quickfix.setTransformationID(StringUtils.trim(child.getFirstChild().getNodeValue()));
            }
        }

        return quickfix;
    }

}
