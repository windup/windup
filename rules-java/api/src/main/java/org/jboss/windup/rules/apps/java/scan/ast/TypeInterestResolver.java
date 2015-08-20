package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TypeInterestResolver
{
    private static TypeInterestResolver defaultInstance;

    private Set<TypeInterest> typeInterestSet = new LinkedHashSet<>();
    private Map<Character, TypeInterestResolver> typeInterestMap = new HashMap<>(26);

    /**
     * Gets the default instance of the {@link TypeInterestResolver}.
     */
    public static TypeInterestResolver defaultInstance()
    {
        if (defaultInstance == null)
        {
            defaultInstance = new TypeInterestResolver();
        }
        return defaultInstance;
    }

    public TypeInterestResolver()
    {
    }

    public void clear()
    {
        this.typeInterestSet.clear();
        this.typeInterestMap.clear();
    }

    public boolean isInteresting(String packageName, String className, String methodName)
    {
        // package should always be at least an empty string
        if (packageName == null)
            packageName = "";

        // class and method should be null if not specified
        if (StringUtils.isEmpty(className))
            className = null;
        if (StringUtils.isEmpty(methodName))
            methodName = null;

        return isInteresting(packageName.toCharArray(), 0, className, methodName);
    }

    private boolean isInteresting(char[] packageName, int currentIndex, String className, String methodName)
    {
        if (currentIndex >= packageName.length)
        {
            return false;
        }

        Character currentCharacter = packageName[currentIndex];
        if (currentCharacter == '.')
            return isInteresting(packageName, currentIndex + 1, className, methodName);

        TypeInterestResolver resolver = typeInterestMap.get(currentCharacter);
        if (resolver == null)
            return false;

        if (!resolver.typeInterestSet.isEmpty())
        {
            for (TypeInterest interest : resolver.typeInterestSet)
            {
                boolean classNameMatch = StringUtils.isBlank(interest.getClassName());
                classNameMatch |= StringUtils.equals(className, interest.getClassName());

                boolean methodNameMatch = StringUtils.isBlank(interest.getMethodName());
                methodNameMatch |= StringUtils.equals(methodName, interest.getMethodName());

                if (classNameMatch && methodNameMatch)
                {
                    return true;
                }
            }
        }

        return resolver.isInteresting(packageName, currentIndex + 1, className, methodName);
    }

    public void addTypeInterest(TypeInterest typeInterest)
    {
        addTypeInterest(typeInterest.getPackagePrefix().toCharArray(), 0, typeInterest);
    }

    private void addTypeInterest(char[] packagePrefix, int currentIndex, TypeInterest typeInterest)
    {
        if (currentIndex >= packagePrefix.length)
        {
            typeInterestSet.add(typeInterest);
            return;
        }

        Character currentCharacter = packagePrefix[currentIndex];
        if (currentCharacter == '.')
            addTypeInterest(packagePrefix, currentIndex + 1, typeInterest);

        TypeInterestResolver resolver = typeInterestMap.get(currentCharacter);
        if (resolver == null)
        {
            resolver = new TypeInterestResolver();
            typeInterestMap.put(currentCharacter, resolver);
        }

        resolver.addTypeInterest(packagePrefix, currentIndex + 1, typeInterest);
    }
}
