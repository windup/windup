/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.graphsearch.GremlinPipelineCriterion;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 *         Provides access to the full GremlinPipeline API:
 *         <ul>
 *         <li><a href="https://github.com/tinkerpop/gremlin/wiki">Gremlin Wiki</a></li>
 *         <li><a href="https://github.com/tinkerpop/gremlin/wiki/Gremlin-Steps">Gremlin Steps (Cheatsheet)</a></li>
 *         <li><a
 *         href="http://www.tinkerpop.com/docs/javadocs/gremlin/2.4.0/com/tinkerpop/gremlin/java/GremlinPipeline.html"
 *         >Gremlin Pipeline Javadoc</a></li>
 *         </ul>
 * 
 */
public interface GremlinPipesQuery
{
    public IterationQueryCriteria endQuery();

    public GremlinPipesQuery addCriterion(GremlinPipelineCriterion criterion);
}
