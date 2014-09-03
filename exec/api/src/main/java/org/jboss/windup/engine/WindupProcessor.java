/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.engine;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface WindupProcessor
{
    /**
     * Executes Windup according to given configuration.
     */
    void execute(WindupProcessorConfig config);

    
    // Convenience / deprecated methods.
    
    /**
     * Executes Windup (including all rules).
     */
    void execute();
}
