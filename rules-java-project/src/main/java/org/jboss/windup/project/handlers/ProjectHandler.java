package org.jboss.windup.project.handlers;

import static org.joox.JOOX.$;

import java.util.List;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "project", namespace = "http://windup.jboss.org/v1/xml")
public class ProjectHandler implements ElementHandler<Project>
{

    @Override
    public Project processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        List<Element> children = $(element).children("artifact").get();
        Artifact artifact = null;
        for (Element child : children)
        {
            artifact = handlerManager.processElement(child);
        }
        
        Project project = Project.dependsOnArtifact(artifact);
        return project;
    }
}
