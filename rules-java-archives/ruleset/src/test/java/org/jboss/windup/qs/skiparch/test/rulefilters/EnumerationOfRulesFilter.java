package org.jboss.windup.qs.skiparch.test.rulefilters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.WindupRuleProvider;

/**
 * A convenient filter for rule providers enumerated as constructor params.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class EnumerationOfRulesFilter implements RuleFilter
{
    private final Set<? extends WindupRuleProvider> classes;
    private final Set<String> classNames = new HashSet();


    public EnumerationOfRulesFilter(Class<? extends WindupRuleProvider> ... classes)
    {
        this.classes = new HashSet(Arrays.asList(classes));
        for( Class<? extends WindupRuleProvider> cls : classes )
            this.classNames.add(cls.getName());
    }


    @Override
    public boolean accept(WindupRuleProvider ruleProvider)
    {
        //return this.classes.contains(ruleProvider.getClass());
        //return this.classNames.contains(ruleProvider.getClass().getName());
        return this.classNames.contains(StringUtils.substringBefore(ruleProvider.toString(), "@"));
    }

}// class
