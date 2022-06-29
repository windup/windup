package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.service.RMIServiceModelService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JAX-WS related annotations, and adds JAX-WS related metadata for these.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = MigrationRulesPhase.class)
public class DiscoverRmiRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverRmiRuleProvider.class);
    private static final String RMI_INHERITANCE = "rmiInheritance";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                .begin()
                .addRule()
                .when(JavaClass
                        .references("java.rmi.Remote")
                        .at(TypeReferenceLocation.IMPORT)
                        .as(RMI_INHERITANCE))
                .perform(Iteration.over(RMI_INHERITANCE).perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                        extractMetadata(event, payload);
                    }
                }).endIteration())
                .withId(ruleIDPrefix + "_RMIInheritanceRule");
    }

    private void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference) {
        // get the rmi interface class from the graph
        JavaClassModel javaClassModel = getJavaClass(typeReference);

        if (!isRemoteInterface(javaClassModel)) {
            LOG.warning("Is not remote: " + javaClassModel.getQualifiedName());
            return;
        }

        LOG.info("Processing: " + typeReference);
        // Make sure we create a source report for the interface source
        typeReference.getFile().setGenerateSourceReport(true);

        RMIServiceModelService rmiService = new RMIServiceModelService(event.getGraphContext());

        if (javaClassModel != null) {
            RMIServiceModel rmiServiceModel = rmiService.getOrCreate(typeReference.getFile().getApplication(), javaClassModel);

            // Create the source report for the RMI Implementation.
            JavaClassService javaClassService = new JavaClassService(event.getGraphContext());

            if (rmiServiceModel != null && rmiServiceModel.getImplementationClass() != null) {
                for (AbstractJavaSourceModel source : javaClassService.getJavaSource(rmiServiceModel.getImplementationClass().getQualifiedName())) {
                    source.setGenerateSourceReport(true);
                }
            }
        }
    }

    public boolean isRemoteInterface(JavaClassModel jcm) {
        if (!jcm.isInterface())
            return false;

        LOG.info("Class: " + jcm.getQualifiedName());
        for (JavaClassModel im : jcm.getInterfaces()) {
            if (StringUtils.equals("java.rmi.Remote", im.getQualifiedName())) {
                return true;
            }
            LOG.info(" - Implements: " + im.getQualifiedName());
        }
        if (jcm.getExtends() != null) {
            LOG.info(" - Extends: " + jcm.getExtends().getQualifiedName());
            return (StringUtils.equals("java.rmi.Remote", jcm.getExtends().getQualifiedName()));
        }
        return false;
    }

    private JavaClassModel getJavaClass(JavaTypeReferenceModel javaTypeReference) {
        JavaClassModel result = null;
        AbstractJavaSourceModel javaSource = javaTypeReference.getFile();

        for (JavaClassModel javaClassModel : javaSource.getJavaClasses()) {
            // there can be only one public one, and the annotated class should be public
            if (javaClassModel.isPublic() != null && javaClassModel.isPublic()) {
                result = javaClassModel;
                break;
            }
        }

        if (result == null) {
            // no public classes found, so try to find any class (even non-public ones)
            result = javaSource.getJavaClasses().iterator().next();
        }
        return result;
    }

    @Override
    public String toString() {
        return "DiscoverEJBAnnotatedClasses";
    }
}
