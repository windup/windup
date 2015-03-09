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

/**
 * Test for @Metadata.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Metadata(id = "myRule2",
            phase = DependentPhase.class,
            after = { Test1EarlierRules.class },
            before = { Test2LaterRules.class },
            tags = { "java", "security" })
public class TestMetadataAnnotationRuleProvider extends SingleRuleProvider
{
    @Override
    public void perform(GraphRewrite event, EvaluationContext evCtx)
    {
        MetadataTestExecutedProviders.executedProvider(this);
        Assert.assertEquals("myRule2", this.getId());
        Assert.assertEquals(DependentPhase.class.getName(), getMetadata().getPhase().getName());

        Assert.assertTrue(getMetadata().getExecuteAfter().contains(Test1EarlierRules.class));
        Assert.assertTrue(getMetadata().hasTags("java", "security"));
    }

}