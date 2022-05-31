package org.jboss.windup.rules.files;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.Context;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Handles parsing the "file-mapping" element to add rules to the current ruleset.
 */
@NamespaceElementHandler(elementName = FileMappingHandler.ELEM_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class FileMappingHandler implements ElementHandler<Void> {
    protected static final String ELEM_NAME = "file-mapping";
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String ON_PARSE_ERROR = "onParseError";
    private static final String ATTR_PARSE_IGNORE = "ignore";
    @Inject
    private GraphTypeManager typeManager;

    @Override
    @SuppressWarnings("unchecked")
    public Void processElement(ParserContext context, Element element) {
        String id = $(element).attr("id");

        String from = $(element).attr(FROM);
        String to = $(element).attr(TO);
        String onParseError = $(element).attr(ON_PARSE_ERROR);

        if (StringUtils.isBlank(from)) {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + FROM + "' attribute");
        }
        if (StringUtils.isBlank(to)) {
            throw new WindupException("The '" + ELEM_NAME + "' element must have a non-empty '" + TO + "' attribute");
        }
        if (!StringUtils.isBlank(onParseError)) {
            final List<String> acceptableValues = Arrays.asList(new String[]{ATTR_PARSE_IGNORE, "warn", "warning"});
            if (!acceptableValues.contains(onParseError))
                throw new WindupException("The '<" + ELEM_NAME + ">' attribute '" + ON_PARSE_ERROR + "' must contain one of: " + StringUtils.join(acceptableValues, ", "));
        }


        List<Class<? extends WindupFrame<?>>> types = new ArrayList<>();
        List<String> typeNames = Arrays.asList(to.trim().split("\\s*,\\s*"));
        for (String name : typeNames) {
            List<Class<? extends WindupFrame<?>>> matchingTypes = new ArrayList<>();
            for (Class<? extends WindupFrame<?>> modelType : typeManager.getRegisteredTypes()) {
                if (modelType.getName().equals(name) ||
                        modelType.getSimpleName().equals(name + ".class") ||
                        modelType.getSimpleName().equals(name + "Model") ||
                        modelType.getSimpleName().equals(name)) {
                    matchingTypes.add(modelType);
                }
            }
            if (matchingTypes.isEmpty()) {
                throw new WindupException("A '" + ELEM_NAME + "' element specifies a file mapping type [" + name
                        + "] that could not be found in any installed addons.");
            }
            if (matchingTypes.size() > 1) {
                throw new WindupException("A '" + ELEM_NAME + "' element specifies a file mapping type ["
                        + name + "] that matched multiple file types. Please select one of matches and "
                        + "update the configuration to use the fully qualified name: ["
                        + matchingTypes.toString().replaceAll(",", System.lineSeparator()) + "]");
            }
            types.addAll(matchingTypes);
        }

        FileMappingTo mappingTo = FileMapping.from(from).to(types.toArray(new Class[types.size()])).onParseError(FileModel.OnParseError.IGNORE);
        Rule rule = StringUtils.isNotBlank(id) ? mappingTo.withId(id) : mappingTo;
        if (rule instanceof Context)
            ((Context) rule).put(RuleMetadataType.RULE_XML, XmlUtil.nodeToString(element));
        context.getBuilder().addRule(rule);
        return null;
    }

}
