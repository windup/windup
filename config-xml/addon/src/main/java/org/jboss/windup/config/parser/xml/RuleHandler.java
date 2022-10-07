package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.w3c.dom.Element;

/**
 * Handles parsing the "rule" element to add rules to the current ruleset.
 */
@NamespaceElementHandler(elementName = "rule", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class RuleHandler implements ElementHandler<Void> {
    @Override
    public Void processElement(ParserContext context, Element element) {
        ConfigurationRuleBuilder rule = (ConfigurationRuleBuilder) context.getBuilder().addRule();
        context.setRule(rule);
        processRuleElement(context, rule, element);
        rule.getRuleBuilder().put(RuleMetadataType.RULE_XML, XmlUtil.nodeToString(element));
        return null;
    }

    /**
     * Processes all of the elements within a rule and attaches this data to the passed in rule. For example, this will process all of the "when",
     * "perform", and "otherwise" elements.
     */
    public static void processRuleElement(ParserContext context, ConfigurationRuleBuilder rule, Element element) {
        String id = $(element).attr("id");

        List<Element> children = $(element).children().get();
        for (Element child : children) {
            Object result = context.processElement(child);

            switch ($(child).tag()) {
                case "when":
                    rule.when(((Condition) result));
                    break;

                case "perform":
                    rule.perform(((Operation) result));
                    break;

                case "otherwise":
                    rule.otherwise(((Operation) result));
                    break;

                case "where":
                    break;
            }
        }

        if (StringUtils.isNotBlank(id)) {
            rule.withId(id);
        }

    }
}
