package org.jboss.windup.config.parser.xml.perform;

import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleHandler;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.config.Operations;
import org.w3c.dom.Element;

import java.util.List;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "perform", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class PerformHandler implements ElementHandler<Operation> {
    @Override
    public Operation processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        OperationBuilder result = Operations.create();
        List<Element> children = $(element).children().get();
        for (Element child : children) {
            switch ($(child).tag()) {
                // special case to deal with rulesubset (nested rules)
                case "rule": {
                    // save the parent Rule
                    ConfigurationRuleBuilder previousRule = handlerManager.getRule();

                    // create the new (nested) rule
                    ConfigurationBuilder builder = ConfigurationBuilder.begin();
                    ConfigurationRuleBuilder newRule = (ConfigurationRuleBuilder) builder.addRule();
                    handlerManager.setRule(newRule);
                    RuleHandler.processRuleElement(handlerManager, newRule, child);
                    // attach this as an operation
                    Operation operation = RuleSubset.create(builder);
                    result = result.and(operation);

                    // set the "current" rule back to the parent rule
                    handlerManager.setRule(previousRule);
                    break;
                }
                default: {
                    Operation operation = handlerManager.processElement(child);
                    result = result.and(operation);
                    break;
                }
            }
        }
        return result;
    }
}
