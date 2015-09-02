package org.jboss.windup.rules.apps.java.scan.ast;

import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.util.ExecutionStatistics;

/**
 * Static store for type interest information. E.g. Which classes to scan and report on.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class TypeInterestFactory
{
    public static synchronized boolean matchesAny(ClassReference reference)
    {
        ExecutionStatistics.get().begin("TypeInterestFactory.matchesAny(text)");
        try
        {
            switch (reference.getLocation())
            {
            case METHOD:
            case METHOD_CALL:
            default:
                // System.out.println("Reference: " + reference.getLine() + ", Loc: " + reference.getLocation());
            }
            return true;
        }
        finally
        {
            ExecutionStatistics.get().end("TypeInterestFactory.matchesAny(text)");
        }
    }
}
