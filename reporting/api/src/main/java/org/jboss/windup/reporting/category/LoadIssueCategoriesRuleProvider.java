package org.jboss.windup.reporting.category;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Loads issue categories into the registry. Also, this is responsible for attaching them to the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = InitializationPhase.class)
public class LoadIssueCategoriesRuleProvider extends AbstractRuleProvider {
    public static final String WINDUP_CATEGORIES_XML_SUFFIX = ".windup.categories.xml";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        loadIssueCategories(ruleLoaderContext);

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        IssueCategoryRegistry.instance(event.getRewriteContext()).attachToGraph(event.getGraphContext());
                    }
                }).withId(LoadIssueCategoriesRuleProvider.class.getSimpleName() + "_attachToGraph");
    }

    private void loadIssueCategories(RuleLoaderContext ruleLoaderContext) {
        if (ruleLoaderContext.getRulePaths() == null)
            return;

        final List<Path> filePaths = new ArrayList<>();
        ruleLoaderContext.getRulePaths().forEach((path) -> {
            try {
                if (!Files.exists(path) || !Files.isReadable(path))
                    return;

                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (Files.isReadable(file) && Files.isRegularFile(file) &&
                                file.getFileName().toString().toLowerCase().endsWith(WINDUP_CATEGORIES_XML_SUFFIX))
                            filePaths.add(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new WindupException("I/O Error during search for issue category files, due to: " + e.getMessage(), e);
            }
        });

        filePaths.forEach((path) -> loadIssueCategory(ruleLoaderContext, path));
    }

    @SuppressWarnings("unchecked")
    private void loadIssueCategory(RuleLoaderContext ruleLoaderContext, Path path) {
        // @formatter:off
        /*
         * Sample xml format:
         *
         * <categories>
         *     <category id=”mandatory-for-ose3” priority=”100”>
         *         <name>Mandatory for OpenShift 3</name>
         *         <description>Items within this category must be addressed for a migration to an OpenShift 3 environment.</description>
         *     </category>
         * </categories>
         */
        // @formatter:on
        try {
            String origin = path.toAbsolutePath().normalize().toString();

            Document doc = new SAXReader().read(path.toFile());
            for (Element element : (List<Element>) doc.getRootElement().elements("category")) {
                String categoryID = element.attributeValue("id");
                String priorityString = element.attributeValue("priority");
                int priority = 0;
                if (StringUtils.isNotEmpty(priorityString)) {
                    try {
                        priority = Integer.valueOf(priorityString);
                    } catch (NumberFormatException e) {
                        String message = "Failed to parse issue category due to malformed priority string: \"" + priorityString + "\"" +
                                " (origin: \"" + origin + "\", id: \"" + categoryID + "\")";
                        throw new WindupException(message);
                    }
                }
                String name = element.elementText("name");
                String description = element.elementText("description");

                IssueCategory issueCategory = new IssueCategory(categoryID, origin, name, description, priority);
                IssueCategoryRegistry.instance(ruleLoaderContext.getContext()).addCategory(issueCategory);
            }
        } catch (DocumentException e) {
            throw new WindupException("Failed to load due to: " + e.getMessage(), e);
        }
    }
}
