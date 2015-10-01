package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.javaee.model.JspSourceFileModel;
import org.jboss.windup.rules.files.FileMapping;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Insures that basic JSP file extensions are mapped to {@link JspSourceFileModel}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JspFileMappingRuleProvider extends AbstractRuleProvider
{

    public JspFileMappingRuleProvider()
    {
        super(MetadataBuilder.forProvider(JspFileMappingRuleProvider.class)
                    .setPhase(ClassifyFileTypesPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule(FileMapping.from(".*\\.jsp$").to(JspSourceFileModel.class))
                    .addRule(FileMapping.from(".*\\.jspx$").to(JspSourceFileModel.class))
                    .addRule(FileMapping.from(".*\\.jsf$").to(JspSourceFileModel.class))
                    .addRule(FileMapping.from(".*\\.xhtml$").to(JspSourceFileModel.class));
    }
}
