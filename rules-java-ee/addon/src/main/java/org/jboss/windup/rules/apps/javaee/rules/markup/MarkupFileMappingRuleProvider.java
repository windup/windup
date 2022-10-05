package org.jboss.windup.rules.apps.javaee.rules.markup;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.rules.apps.javaee.model.CssFileModel;
import org.jboss.windup.rules.apps.javaee.model.HtmlFileModel;
import org.jboss.windup.rules.apps.javaee.model.JsFileModel;
import org.jboss.windup.rules.files.FileMapping;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Discovers .html files from the applications being analyzed.
 */
@RuleMetadata(phase = ClassifyFileTypesPhase.class)
public class MarkupFileMappingRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule(FileMapping.from(".*\\.html$").to(HtmlFileModel.class))
                .addRule(FileMapping.from(".*\\.css$").to(CssFileModel.class))
                .addRule(FileMapping.from(".*\\.js$").to(JsFileModel.class));
    }
}
