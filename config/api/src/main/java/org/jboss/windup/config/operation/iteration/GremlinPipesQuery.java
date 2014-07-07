/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.Tokens.T;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import com.tinkerpop.pipes.transform.TransformPipe;
import com.tinkerpop.pipes.util.structures.Pair;
import com.tinkerpop.pipes.util.structures.Table;
import com.tinkerpop.pipes.util.structures.Tree;

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

    public GremlinPipesQuery step(final PipeFunction function);

    public GremlinPipesQuery step(final Pipe<Vertex, Vertex> pipe);

    public GremlinPipesQuery copySplit(final Pipe<Vertex, Vertex>... pipes);

    public GremlinPipesQuery exhaustMerge();

    public GremlinPipesQuery fairMerge();

    public GremlinPipesQuery ifThenElse(final PipeFunction<Vertex, Boolean> ifFunction,
                final PipeFunction<Vertex, Vertex> thenFunction, final PipeFunction<Vertex, Vertex> elseFunction);

    public GremlinPipesQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction);

    public GremlinPipesQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction);

    public GremlinPipesQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction);

    public GremlinPipesQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction);

    public GremlinPipesQuery and(final Pipe<Vertex, ?>... pipes);

    public GremlinPipesQuery back(final int numberedStep);

    public GremlinPipesQuery back(final String namedStep);

    public GremlinPipesQuery dedup();

    public GremlinPipesQuery dedup(final PipeFunction<Vertex, ?> dedupFunction);

    public GremlinPipesQuery except(final Collection<Vertex> collection);

    public GremlinPipesQuery except(final String... namedSteps);

    public GremlinPipesQuery filter(final PipeFunction<Vertex, Boolean> filterFunction);

    public GremlinPipesQuery or(final Pipe<Vertex, ?>... pipes);

    public GremlinPipesQuery random(final Double bias);

    public GremlinPipesQuery range(final int low, final int high);

    public GremlinPipesQuery retain(final Collection<Vertex> collection);

    public GremlinPipesQuery retain(final String... namedSteps);

    public GremlinPipesQuery simplePath();

    public GremlinPipesQuery has(final String key);

    public GremlinPipesQuery hasNot(final String key);

    public GremlinPipesQuery has(final String key, final Object value);

    public GremlinPipesQuery has(final String key, final Tokens.T compareToken, final Object value);

    public GremlinPipesQuery has(final String key, final Predicate predicate, final Object value);

    public GremlinPipesQuery hasNot(final String key, final Object value);

    public GremlinPipesQuery interval(final String key, final Comparable startValue, final Comparable endValue);

    public GremlinPipesQuery gather();

    public GremlinPipesQuery gather(final PipeFunction<List, ?> function);

    public GremlinPipesQuery _();

    public GremlinPipesQuery memoize(final String namedStep);

    public GremlinPipesQuery memoize(final int numberedStep);

    public GremlinPipesQuery memoize(final String namedStep, final Map map);

    public GremlinPipesQuery memoize(final int numberedStep, final Map map);

    public GremlinPipesQuery order();

    public GremlinPipesQuery order(TransformPipe.Order order);

    public GremlinPipesQuery order(final PipeFunction<Pair<Vertex, Vertex>, Integer> compareFunction);

    public GremlinPipesQuery path(final PipeFunction... pathFunctions);

    public GremlinPipesQuery scatter();

    public GremlinPipesQuery select(final Collection<String> stepNames, final PipeFunction... columnFunctions);

    public GremlinPipesQuery select(final PipeFunction... columnFunctions);

    public GremlinPipesQuery select();

    public GremlinPipesQuery shuffle();

    public GremlinPipesQuery cap();

    public GremlinPipesQuery orderMap(TransformPipe.Order order);

    public GremlinPipesQuery orderMap(PipeFunction<Pair<Map.Entry, Map.Entry>, Integer> compareFunction);

    public GremlinPipesQuery transform(final PipeFunction<Vertex, T> function);

    public GremlinPipesQuery bothE(final String... labels);

    public GremlinPipesQuery bothE(final int branchFactor, final String... labels);

    public GremlinPipesQuery both(final String... labels);

    public GremlinPipesQuery both(final int branchFactor, final String... labels);

    public GremlinPipesQuery bothV();

    public GremlinPipesQuery idEdge(final Graph graph);

    public GremlinPipesQuery id();

    public GremlinPipesQuery idVertex(final Graph graph);

    public GremlinPipesQuery inE(final String... labels);

    public GremlinPipesQuery inE(final int branchFactor, final String... labels);

    public GremlinPipesQuery in(final String... labels);

    public GremlinPipesQuery in(final int branchFactor, final String... labels);

    public GremlinPipesQuery inV();

    public GremlinPipesQuery label();

    public GremlinPipesQuery outE(final String... labels);

    public GremlinPipesQuery outE(final int branchFactor, final String... labels);

    public GremlinPipesQuery out(final String... labels);

    public GremlinPipesQuery out(final int branchFactor, final String... labels);

    public GremlinPipesQuery outV();

    public GremlinPipesQuery map(final String... keys);

    public GremlinPipesQuery property(final String key);

    public GremlinPipesQuery aggregate();

    public GremlinPipesQuery aggregate(final Collection<Vertex> aggregate);

    public GremlinPipesQuery aggregate(final Collection aggregate, final PipeFunction<Vertex, ?> aggregateFunction);

    public GremlinPipesQuery aggregate(final PipeFunction<Vertex, ?> aggregateFunction);

    public GremlinPipesQuery optional(final int numberedStep);

    public GremlinPipesQuery optional(final String namedStep);

    public GremlinPipesQuery groupBy(final Map<?, List<?>> map, final PipeFunction keyFunction,
                final PipeFunction valueFunction);

    public GremlinPipesQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction);

    public GremlinPipesQuery groupBy(final Map reduceMap, final PipeFunction keyFunction,
                final PipeFunction valueFunction, final PipeFunction reduceFunction);

    public GremlinPipesQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction,
                final PipeFunction reduceFunction);

    public GremlinPipesQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction);

    public GremlinPipesQuery groupCount(final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction);

    public GremlinPipesQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction);

    public GremlinPipesQuery groupCount(final PipeFunction keyFunction);

    public GremlinPipesQuery groupCount(final Map<?, Number> map);

    public GremlinPipesQuery groupCount();

    public GremlinPipesQuery sideEffect(final PipeFunction<Vertex, ?> sideEffectFunction);

    public GremlinPipesQuery store(final Collection<Vertex> storage);

    public GremlinPipesQuery store(final Collection storage, final PipeFunction<Vertex, ?> storageFunction);

    public GremlinPipesQuery store();

    public GremlinPipesQuery store(final PipeFunction<Vertex, ?> storageFunction);

    public GremlinPipesQuery table(final Table table, final Collection<String> stepNames,
                final PipeFunction... columnFunctions);

    public GremlinPipesQuery table(final Table table, final PipeFunction... columnFunctions);

    public GremlinPipesQuery table(final PipeFunction... columnFunctions);

    public GremlinPipesQuery table(final Table table);

    public GremlinPipesQuery table();

    public GremlinPipesQuery tree(final Tree tree, final PipeFunction... branchFunctions);

    public GremlinPipesQuery tree(final PipeFunction... branchFunctions);

    public GremlinPipesQuery linkOut(final String label, final String namedStep);

    public GremlinPipesQuery linkIn(final String label, final String namedStep);

    public GremlinPipesQuery linkBoth(final String label, final String namedStep);

    public GremlinPipesQuery linkOut(final String label, final Vertex other);

    public GremlinPipesQuery linkIn(final String label, final Vertex other);

    public GremlinPipesQuery linkBoth(final String label, final Vertex other);

    public GremlinPipesQuery named(final String name);

    public GremlinPipesQuery enablePath();

    public GremlinPipesQuery cast(Class<Vertex> end);
}
