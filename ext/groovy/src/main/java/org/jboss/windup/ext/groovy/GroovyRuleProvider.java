/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.ext.groovy;

import java.util.List;

import javax.enterprise.inject.Vetoed;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
@Vetoed
public class GroovyRuleProvider extends WindupRuleProvider
{
    private static Logger LOG = LoggerFactory.getLogger(GroovyRuleProvider.class);

    private String ruleID;
    private RulePhase rulePhase;
    private List<String> ruleDependencies;
    private ConfigurationRuleBuilder completedConfiguration = null;

    public GroovyRuleProvider(String ruleID, RulePhase rulePhase, List<String> ruleDependencies,
                ConfigurationBuilder originalConfigBuilder)
    {
        this.ruleID = ruleID;
        this.rulePhase = rulePhase;
        this.ruleDependencies = ruleDependencies;

        ConfigurationBuilder newConfigBuilder = ConfigurationBuilder.begin();
        for (Rule rule : originalConfigBuilder.getRules())
        {
            if (completedConfiguration == null)
            {
                completedConfiguration = newConfigBuilder.addRule(rule);
            }
            else
            {
                completedConfiguration = completedConfiguration.addRule(rule);
            }
        }
    }

    @Override
    public String getID()
    {
        return ruleID;
    }

    @Override
    public List<String> getIDDependencies()
    {
        return ruleDependencies;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {

        return completedConfiguration;
    }

    @Override
    public RulePhase getPhase()
    {
        return rulePhase;
    }

}
