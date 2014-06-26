/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.foreach;

import org.ocpsoft.rewrite.config.Operation;

/**
 * @author Ondrej Zizka, ozizka@redhat.com
 */
public interface ForBuilderPerform
{
    ForBuilderOtherwise otherwise(Operation operation);
    
    ForBuilderComplete endFor();
}
