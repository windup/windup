/**
 * Copyright 2004 - 2017 Syncleus, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.windup.graph;

import java.util.HashSet;
import java.util.Set;

import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.annotation.AdjacencyMethodHandler;
import com.syncleus.ferma.framefactories.annotation.InVertexMethodHandler;
import com.syncleus.ferma.framefactories.annotation.IncidenceMethodHandler;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import com.syncleus.ferma.framefactories.annotation.OutVertexMethodHandler;
import com.syncleus.ferma.framefactories.annotation.PropertyMethodHandler;

public class AnnotationFrameFactory extends AbstractAnnotationFrameFactory {

    public AnnotationFrameFactory(final ClassLoader classLoader, final ReflectionCache reflectionCache) {
        super(classLoader, reflectionCache, collectHandlers(null));
    }

    /**
     * Subclasses can use this constructor to add additional custom method handlers.
     *
     * @param reflectionCache The reflection cache used to inspect annotations.
     * @param handlers The handlers used to generate new annotation support.
     */
    protected AnnotationFrameFactory(final ClassLoader classLoader, final ReflectionCache reflectionCache, Set<MethodHandler> handlers) {
        super(classLoader, reflectionCache, collectHandlers(handlers));
    }

    private static final Set<MethodHandler> collectHandlers(Set<MethodHandler> additionalHandlers) {
        final Set<MethodHandler> methodHandlers = new HashSet<>();

        final PropertyMethodHandler propertyHandler = new PropertyMethodHandler();
        methodHandlers.add(propertyHandler);

        final InVertexMethodHandler inVertexHandler = new InVertexMethodHandler();
        methodHandlers.add(inVertexHandler);

        final OutVertexMethodHandler outVertexHandler = new OutVertexMethodHandler();
        methodHandlers.add(outVertexHandler);

        final AdjacencyMethodHandler adjacencyHandler = new AdjacencyMethodHandler();
        methodHandlers.add(adjacencyHandler);

        final IncidenceMethodHandler incidenceHandler = new IncidenceMethodHandler();
        methodHandlers.add(incidenceHandler);

        if (additionalHandlers != null)
            methodHandlers.addAll(additionalHandlers);

        return methodHandlers;
    }

}
