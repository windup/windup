package org.jboss.windup.rules.apps.javaee.rules;


import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;

import java.util.stream.Collectors;

@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public abstract class DiscoverAnnotatedClassRuleProvider extends AbstractRuleProvider {
    protected String getAnnotationLiteralValue(JavaAnnotationTypeReferenceModel model, String name) {
        JavaAnnotationTypeValueModel valueModel = model.getAnnotationValues().get(name);
        if (valueModel instanceof JavaAnnotationLiteralTypeValueModel) {
            JavaAnnotationLiteralTypeValueModel literalTypeValue = (JavaAnnotationLiteralTypeValueModel) valueModel;
            return literalTypeValue.getLiteralValue();
        } else if (valueModel instanceof JavaAnnotationListTypeValueModel) {
            JavaAnnotationListTypeValueModel listTypeValueModel = (JavaAnnotationListTypeValueModel) valueModel;
            return listTypeValueModel.getList().stream().map(e-> ((JavaAnnotationLiteralTypeValueModel) e).getLiteralValue()).collect(Collectors.joining(","));
        }
        else {
            return null;
        }
    }

}