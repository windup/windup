package org.jboss.windup.qs.skiparch.test.rulefilters;

import org.jboss.windup.config.WindupRuleProvider;

/**
 * Accepts rule providers that belong under certain package and "subpackages".
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class PackageSubtreeRulesFilter extends PackageRulesFilter
{
    public PackageSubtreeRulesFilter(String... packages)
    {
        super(packages);
    }

    public PackageSubtreeRulesFilter(Package pkg)
    {
        super(pkg);
    }

    public PackageSubtreeRulesFilter(Class<? extends WindupRuleProvider> pkgCls)
    {
        this(pkgCls.getPackage());
    }

    @Override
    public boolean accept(WindupRuleProvider ruleProvider)
    {
        final Package cls = ruleProvider.getClass().getPackage();
        if (cls == null)
            return false;
        
        String rulePkg = cls.getName();

        for (String pkg : packages)
            if (rulePkg.startsWith(pkg) && (rulePkg.equals(pkg) || rulePkg.charAt(rulePkg.length()) == '.') )
                return true;

        return false;
    }

}// class
