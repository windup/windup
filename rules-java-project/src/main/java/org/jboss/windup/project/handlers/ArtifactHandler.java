package org.jboss.windup.project.handlers;

import static org.joox.JOOX.$;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Version;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "artifact", namespace = "http://windup.jboss.org/v1/xml")
public class ArtifactHandler implements ElementHandler<Artifact>
{

    @Override
    public Artifact processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        String groupId = $(element).attr("groupId");
        String artifactId = $(element).attr("artifactId");
        String from = $(element).attr("from");
        String to = $(element).attr("to");
        Artifact artifact = Artifact.withGroupId(groupId).andArtifactId(artifactId).andVersion(Version.fromVersion(from).to(to));
        return artifact;
    }
}
