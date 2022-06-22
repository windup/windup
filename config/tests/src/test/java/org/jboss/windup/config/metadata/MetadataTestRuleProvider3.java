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
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RuleMetadata(id = "myRule1",
        phase = DependentPhase.class,
        after = {MetadataTestRuleProvider1.class},
        before = {MetadataTestRuleProvider2.class},
        tags = {"java", "security"})
public class MetadataTestRuleProvider3 extends SingleRuleProvider {
    @Override
    public void perform(GraphRewrite event, EvaluationContext evCtx) {
        ExecutedProviders.executedProvider(this);
        Assert.assertEquals("myRule1", this.getId());
        Assert.assertEquals(DependentPhase.class, this.getMetadata().getPhase());

        Assert.assertTrue(this.getMetadata().getExecuteAfter().contains(MetadataTestRuleProvider1.class));

        RuleProviderRegistry providerRegistry = RuleProviderRegistry.instance(event);
        Assert.assertNotNull(providerRegistry);

        Assert.assertTrue(getMetadata().hasTags("java", "security"));

        Set<TechnologyReference> sourceTechnologies = getMetadata().getSourceTechnologies();
        Assert.assertEquals(2, sourceTechnologies.size());
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("source-a", "[1,]")));
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("source-b", "[2,]")));

        Set<TechnologyReference> targetTechnologies = getMetadata().getTargetTechnologies();
        Assert.assertEquals(1, targetTechnologies.size());
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("target-x", "[3,]")));
    }

}