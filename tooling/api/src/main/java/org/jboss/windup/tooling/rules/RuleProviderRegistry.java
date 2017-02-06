package org.jboss.windup.tooling.rules;

import java.io.Serializable;
import java.util.List;

public interface RuleProviderRegistry extends Serializable
{
    List<RuleProvider> getRuleProviders();
}
