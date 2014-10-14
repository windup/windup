/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.exec;

import org.jboss.windup.exec.configuration.WindupConfiguration;

/**
 * The entry point of the Windup engine.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface WindupProcessor
{
    /**
     * Executes Windup using the given {@link WindupConfiguration}.
     */
    void execute(WindupConfiguration config);

    /**
     * Executes Windup (including all rules found in loaded addons).
     */
    void execute();
}
