package org.jboss.windup.qs.skiparch.test.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * A convenient filter for rule providers enumerated as constructor params.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PackageRulesFilter implements RuleFilter
{
    protected Set<String> packages;


    public PackageRulesFilter(String ... packages)
    {
        this.packages = new HashSet(Arrays.asList(packages));
    }


    public PackageRulesFilter(Package pkg)
    {
        this.packages = new HashSet(Arrays.asList(pkg.getName()));
    }


    @Override
    public boolean accept(WindupRuleProvider ruleProvider)
    {
        return this.packages.contains(ruleProvider.getClass().getPackage().getName());
    }

}// class
