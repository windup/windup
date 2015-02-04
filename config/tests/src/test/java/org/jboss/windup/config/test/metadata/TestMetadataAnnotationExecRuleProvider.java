/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.test.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Rules;
import org.jboss.windup.config.metadata.WindupRuleMetadata;
import org.jboss.windup.config.phase.Implicit;
import org.junit.Assert;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Test for @Rules.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Rules( id = "myRule1",
    phase = Implicit.class,
    after = { Test1EarlierRules.class },
    before = { Test2LaterRules.class },
    categories = {"java", "security"}
)
public class TestMetadataAnnotationExecRuleProvider extends SingleOpRuleProvider
{
    @Override
    public void perform(GraphRewrite event, EvaluationContext evCtx)
    {
        Assert.assertEquals("myRule1", this.getID());
        Assert.assertEquals(Implicit.class.getName(), this.getPhase().getName());

        Assert.assertFalse("@Rules after is not empty", this.getExecuteAfter().isEmpty());
        Assert.assertTrue("@Rules after = {Test1EarlierRules.class}", this.getExecuteAfter().contains(Test1EarlierRules.class));

        WindupRuleMetadata wrm = (WindupRuleMetadata) event.getRewriteContext().get(WindupRuleMetadata.class);
        Assert.assertNotNull("event.getRewriteContext()[WindupRuleMetadata.class] is not null", wrm);
        for(WindupRuleProvider provider : wrm.getProviders())
        {
            if(provider.getClass().equals(TestMetadataAnnotationExecRuleProvider.class))
                continue;
        }

        Object cat = event.getRewriteContext().get(RuleMetadata.CATEGORY);

        // This doesn't work - TODO - see mail from 4th Feb.
        //Assert.assertNotNull("CATEGORY is set", cat);
        //Assert.assertTrue("CATEGORY = 'java,security'", cat.equals("java,security"));
    }

}
