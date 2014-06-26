/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.foreach;

import org.jboss.windup.config.operation.iteration.GremlinPipesQuery;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * @author Ondrej Zizka, ozizka@redhat.com
 */
public interface ForBuilderEach
{
    /**
     * Iterate over the set of framed vertices in given variable.
     */
    public ForBuilderIn in(Class<? extends WindupVertexFrame> varType, String var);

    /**
     * Iterate over the set of framed vertices in given variable.
     */
    public ForBuilderIn in(String var);

    /**
     * Starts a gremlin query part.
     * Refer to Gremlin Pipes for the subsequent API.
     */
    public GremlinPipesQuery queryFor();
    
    /**
     * Performs a Gremlin query per given string.
     * Refer to Gremlin for the query syntax.
     */
    public GremlinQuery from( String gremlinQuery );
    
}
