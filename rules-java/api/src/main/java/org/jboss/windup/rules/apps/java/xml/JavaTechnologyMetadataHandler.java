package org.jboss.windup.rules.apps.java.xml;

import static org.joox.JOOX.$;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.metadata.TechnologyMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.metadata.MetadataTechnologyHandler;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.rules.apps.java.JavaTechnologyMetadata;
import org.w3c.dom.Element;

/**
 * Handler for the <java-technology-metadata> element.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = JavaTechnologyMetadataHandler.ELEM_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class JavaTechnologyMetadataHandler implements ElementHandler<TechnologyMetadata> {
    public static final String ELEM_NAME = "java-technology-metadata";
    private static final String ADDITIONAL_CLASSPATH = "additional-classpath";

    @Override
    public TechnologyMetadata processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        // create an anonymous class, because the technology handler is abstract.
        TechnologyReference technologyReference = new MetadataTechnologyHandler() {
        }.processElement(handlerManager, $(element).child("technology").get(0));

        JavaTechnologyMetadata javaTechnologyMetadata = new JavaTechnologyMetadata(technologyReference);
        List<Element> children = $(element).children(ADDITIONAL_CLASSPATH).get();
        for (Element child : children) {
            String additionalClasspath = child.getTextContent();
            additionalClasspath = FilenameUtils.separatorsToSystem(additionalClasspath);

            Path path = handlerManager.getXmlInputPath().getParent().resolve(additionalClasspath);
            javaTechnologyMetadata.addAdditionalClasspath(path);
        }

        return javaTechnologyMetadata;
    }
}
