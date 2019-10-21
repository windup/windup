package org.jboss.windup.rules.apps.java.xml;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.java.condition.Dependency;
import org.jboss.windup.rules.apps.java.condition.Version;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "dependency", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class DependencyHandler implements ElementHandler<Dependency>
{
    @Override
    public Dependency processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String groupId = $(element).attr("groupId");
        String artifactId = $(element).attr("artifactId");
        String from = $(element).attr("fromVersion");
        String to = $(element).attr("toVersion");
        return Dependency.withGroupId(groupId).andArtifactId(artifactId).andVersion(Version.fromVersion(from).to(to));
    }
}
