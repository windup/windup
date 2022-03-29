package org.jboss.windup.project.handlers;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.graph.model.DependencyLocation;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.rules.apps.java.condition.Version;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.joox.JOOX.$;

@NamespaceElementHandler(elementName = "artifact", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class ArtifactHandler implements ElementHandler<Artifact>
{

    @Override
    public Artifact processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String groupId = $(element).attr("groupId");
        String artifactId = $(element).attr("artifactId");
        String from = $(element).attr("fromVersion");
        String to = $(element).attr("toVersion");

        List<Element> locationElements = $(element).children().get();

        Collection<DependencyLocation> locations = null;
        if (locationElements != null) {
             locations = locationElements.stream().map(child -> DependencyLocation.valueOf(child.getTextContent())).collect(toSet());
        }

        return Artifact.withGroupId(groupId).andArtifactId(artifactId).andVersion(Version.fromVersion(from).to(to)).andLocations(locations);
    }
}
