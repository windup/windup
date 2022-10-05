/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.metadata;

import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.ruleprovider.SingleRuleProvider;
import org.junit.Assert;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Test for {@link RuleMetadata}.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@RuleMetadata(id = "myRule2",
        phase = DependentPhase.class,
        after = {MetadataTestRuleProvider1.class},
        before = {MetadataTestRuleProvider2.class},
        tags = {"java", "security"},
        sourceTechnologies = {
                @Technology(id = "ejb", versionRange = "[1,2)")
        },
        targetTechnologies = {
                @Technology(id = "ejb", versionRange = "[3,)")
        })
public class MetadataTestRuleProvider4 extends SingleRuleProvider {
    @Override
    public void perform(GraphRewrite event, EvaluationContext evCtx) {
        ExecutedProviders.executedProvider(this);
        Assert.assertEquals("myRule2", this.getId());
        Assert.assertEquals(DependentPhase.class.getName(), getMetadata().getPhase().getName());

        Assert.assertTrue(getMetadata().getExecuteAfter().contains(MetadataTestRuleProvider1.class));
        Assert.assertTrue(getMetadata().hasTags("java", "security"));
        Assert.assertFalse(getMetadata().hasTags("ruleset-meta-tag"));

        Set<TechnologyReference> sourceTechnologies = getMetadata().getSourceTechnologies();
        Assert.assertEquals(1, sourceTechnologies.size());
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("ejb", "[1,2)")));

        Set<TechnologyReference> targetTechnologies = getMetadata().getTargetTechnologies();
        Assert.assertEquals(1, targetTechnologies.size());
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("ejb", "[3,)")));

        Assert.assertTrue(getMetadata().getRequiredAddons().isEmpty());
    }
}