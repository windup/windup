/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.test.metadata;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Rules;
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
public class TestMetadataAnnotationRuleProvider extends SingleOpRuleProvider
{
    @Override
    public void perform(GraphRewrite event, EvaluationContext evCtx)
    {
        Assert.assertEquals("myRule1", this.getID());
        Assert.assertEquals(Implicit.class.getName(), this.getPhase().getName());

        Assert.assertFalse("@Rules after is not empty", this.getExecuteAfter().isEmpty());
        Assert.assertTrue("@Rules after = {Test1EarlierRules.class}", this.getExecuteAfter().contains(Test1EarlierRules.class));

        /*// Can only be tested in the WindupExecutor-based test.
        Object cat = event.getRewriteContext().get(RuleMetadata.CATEGORY);
        Assert.assertNotNull("CATEGORY is set", cat);
        Assert.assertTrue("CATEGORY = 'java,security'", cat.equals("java,security"));
        */
    }

}
