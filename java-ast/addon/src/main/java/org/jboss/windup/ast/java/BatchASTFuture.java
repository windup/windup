package org.jboss.windup.ast.java;

/**
 * Used for the AST Processor to indicate that it is done processing.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface BatchASTFuture {
    /**
     * Returns true if all processing is completed, false otherwise.
     */
    boolean isDone();
}
