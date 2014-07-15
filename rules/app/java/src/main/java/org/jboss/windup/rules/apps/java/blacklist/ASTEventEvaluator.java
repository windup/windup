package org.jboss.windup.rules.apps.java.blacklist;

import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;

/**
 * Object reacting to the fired {@link JavaScannerASTEvent} event.
 * @author mbriskar
 *
 */
public interface ASTEventEvaluator
{
    /**
     * Reaction to the {@link JavaScannerASTEvent} event.
     * @author mbriskar
     *
     */
    public abstract void evaluateASTEvent(JavaScannerASTEvent event);
}
