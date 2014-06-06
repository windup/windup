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
public interface IterationQuery
{
    public IterationQueryCriteria endQuery();

    public IterationQuery step(final PipeFunction function);

    public IterationQuery step(final Pipe<Vertex, Vertex> pipe);

    public IterationQuery copySplit(final Pipe<Vertex, Vertex>... pipes);

    public IterationQuery exhaustMerge();

    public IterationQuery fairMerge();

    public IterationQuery ifThenElse(final PipeFunction<Vertex, Boolean> ifFunction,
                final PipeFunction<Vertex, Vertex> thenFunction, final PipeFunction<Vertex, Vertex> elseFunction);

    public IterationQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction);

    public IterationQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction);

    public IterationQuery loop(final int numberedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction);

    public IterationQuery loop(final String namedStep,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> whileFunction,
                final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> emitFunction);

    public IterationQuery and(final Pipe<Vertex, ?>... pipes);

    public IterationQuery back(final int numberedStep);

    public IterationQuery back(final String namedStep);

    public IterationQuery dedup();

    public IterationQuery dedup(final PipeFunction<Vertex, ?> dedupFunction);

    public IterationQuery except(final Collection<Vertex> collection);

    public IterationQuery except(final String... namedSteps);

    public IterationQuery filter(final PipeFunction<Vertex, Boolean> filterFunction);

    public IterationQuery or(final Pipe<Vertex, ?>... pipes);

    public IterationQuery random(final Double bias);

    public IterationQuery range(final int low, final int high);

    public IterationQuery retain(final Collection<Vertex> collection);

    public IterationQuery retain(final String... namedSteps);

    public IterationQuery simplePath();

    public IterationQuery has(final String key);

    public IterationQuery hasNot(final String key);

    public IterationQuery has(final String key, final Object value);

    public IterationQuery has(final String key, final Tokens.T compareToken, final Object value);

    public IterationQuery has(final String key, final Predicate predicate, final Object value);

    public IterationQuery hasNot(final String key, final Object value);

    public IterationQuery interval(final String key, final Comparable startValue, final Comparable endValue);

    public IterationQuery gather();

    public IterationQuery gather(final PipeFunction<List, ?> function);

    public IterationQuery _();

    public IterationQuery memoize(final String namedStep);

    public IterationQuery memoize(final int numberedStep);

    public IterationQuery memoize(final String namedStep, final Map map);

    public IterationQuery memoize(final int numberedStep, final Map map);

    public IterationQuery order();

    public IterationQuery order(TransformPipe.Order order);

    public IterationQuery order(final PipeFunction<Pair<Vertex, Vertex>, Integer> compareFunction);

    public IterationQuery path(final PipeFunction... pathFunctions);

    public IterationQuery scatter();

    public IterationQuery select(final Collection<String> stepNames, final PipeFunction... columnFunctions);

    public IterationQuery select(final PipeFunction... columnFunctions);

    public IterationQuery select();

    public IterationQuery shuffle();

    public IterationQuery cap();

    public IterationQuery orderMap(TransformPipe.Order order);

    public IterationQuery orderMap(PipeFunction<Pair<Map.Entry, Map.Entry>, Integer> compareFunction);

    public IterationQuery transform(final PipeFunction<Vertex, T> function);

    public IterationQuery bothE(final String... labels);

    public IterationQuery bothE(final int branchFactor, final String... labels);

    public IterationQuery both(final String... labels);

    public IterationQuery both(final int branchFactor, final String... labels);

    public IterationQuery bothV();

    public IterationQuery idEdge(final Graph graph);

    public IterationQuery id();

    public IterationQuery idVertex(final Graph graph);

    public IterationQuery inE(final String... labels);

    public IterationQuery inE(final int branchFactor, final String... labels);

    public IterationQuery in(final String... labels);

    public IterationQuery in(final int branchFactor, final String... labels);

    public IterationQuery inV();

    public IterationQuery label();

    public IterationQuery outE(final String... labels);

    public IterationQuery outE(final int branchFactor, final String... labels);

    public IterationQuery out(final String... labels);

    public IterationQuery out(final int branchFactor, final String... labels);

    public IterationQuery outV();

    public IterationQuery map(final String... keys);

    public IterationQuery property(final String key);

    public IterationQuery aggregate();

    public IterationQuery aggregate(final Collection<Vertex> aggregate);

    public IterationQuery aggregate(final Collection aggregate, final PipeFunction<Vertex, ?> aggregateFunction);

    public IterationQuery aggregate(final PipeFunction<Vertex, ?> aggregateFunction);

    public IterationQuery optional(final int numberedStep);

    public IterationQuery optional(final String namedStep);

    public IterationQuery groupBy(final Map<?, List<?>> map, final PipeFunction keyFunction,
                final PipeFunction valueFunction);

    public IterationQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction);

    public IterationQuery groupBy(final Map reduceMap, final PipeFunction keyFunction,
                final PipeFunction valueFunction, final PipeFunction reduceFunction);

    public IterationQuery groupBy(final PipeFunction keyFunction, final PipeFunction valueFunction,
                final PipeFunction reduceFunction);

    public IterationQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction);

    public IterationQuery groupCount(final PipeFunction keyFunction,
                final PipeFunction<Pair<?, Number>, Number> valueFunction);

    public IterationQuery groupCount(final Map<?, Number> map, final PipeFunction keyFunction);

    public IterationQuery groupCount(final PipeFunction keyFunction);

    public IterationQuery groupCount(final Map<?, Number> map);

    public IterationQuery groupCount();

    public IterationQuery sideEffect(final PipeFunction<Vertex, ?> sideEffectFunction);

    public IterationQuery store(final Collection<Vertex> storage);

    public IterationQuery store(final Collection storage, final PipeFunction<Vertex, ?> storageFunction);

    public IterationQuery store();

    public IterationQuery store(final PipeFunction<Vertex, ?> storageFunction);

    public IterationQuery table(final Table table, final Collection<String> stepNames,
                final PipeFunction... columnFunctions);

    public IterationQuery table(final Table table, final PipeFunction... columnFunctions);

    public IterationQuery table(final PipeFunction... columnFunctions);

    public IterationQuery table(final Table table);

    public IterationQuery table();

    public IterationQuery tree(final Tree tree, final PipeFunction... branchFunctions);

    public IterationQuery tree(final PipeFunction... branchFunctions);

    public IterationQuery linkOut(final String label, final String namedStep);

    public IterationQuery linkIn(final String label, final String namedStep);

    public IterationQuery linkBoth(final String label, final String namedStep);

    public IterationQuery linkOut(final String label, final Vertex other);

    public IterationQuery linkIn(final String label, final Vertex other);

    public IterationQuery linkBoth(final String label, final Vertex other);

    public IterationQuery as(final String name);

    public IterationQuery enablePath();

    public IterationQuery cast(Class<Vertex> end);
}
