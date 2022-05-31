package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.rules.apps.javaee.model.JspSourceFileModel;
import org.jboss.windup.rules.files.FileMapping;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Insures that basic JSP file extensions are mapped to {@link JspSourceFileModel}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RuleMetadata(phase = ClassifyFileTypesPhase.class)
public class JspFileMappingRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule(FileMapping.from(".*\\.jsp$").to(JspSourceFileModel.class))
                .addRule(FileMapping.from(".*\\.jspx$").to(JspSourceFileModel.class))
                .addRule(FileMapping.from(".*\\.tag$").to(JspSourceFileModel.class))
                .addRule(FileMapping.from(".*\\.tagx$").to(JspSourceFileModel.class))
                ;
    }
}
