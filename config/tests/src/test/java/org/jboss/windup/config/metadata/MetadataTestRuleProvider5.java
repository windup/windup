/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.ruleprovider.SingleRuleProvider;
import org.junit.Assert;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Set;

/**
 * Test for {@link RuleMetadata}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@RuleMetadata(id = "myRule2",
        phase = DependentPhase.class,
        after = {MetadataTestRuleProvider3.class},
        tags = {"java", "security"},
        sourceTechnologies = {
                @Technology(id = "ejb", versionRange = "[1,2)")
        },
        targetTechnologies = {
                @Technology(id = "ejb", versionRange = "[3,)")
        })
public class MetadataTestRuleProvider5 extends SingleRuleProvider {
    @Override
    public void perform(GraphRewrite event, EvaluationContext evCtx) {
        ExecutedProviders.executedProvider(this);
        Assert.assertEquals("myRule2", this.getId());
        Assert.assertEquals(DependentPhase.class.getName(), getMetadata().getPhase().getName());

        Assert.assertTrue(getMetadata().getExecuteBefore().isEmpty());
        Assert.assertTrue(getMetadata().getExecuteAfter().contains(MetadataTestRuleProvider3.class));
        Assert.assertTrue(getMetadata().hasTags("java", "security", "ruleset-meta-tag"));

        Set<TechnologyReference> sourceTechnologies = getMetadata().getSourceTechnologies();
        Assert.assertEquals(3, sourceTechnologies.size());
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("ejb", "[1,2)")));
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("source-a", "[1,]")));
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("source-b", "[2,]")));

        Set<TechnologyReference> targetTechnologies = getMetadata().getTargetTechnologies();
        Assert.assertEquals(2, targetTechnologies.size());
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("ejb", "[3,)")));
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("target-x", "[3,]")));

        Assert.assertFalse(getMetadata().getRequiredAddons().isEmpty());
    }
}