/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.operation.iteration;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Retrieves proper Iterable of frames, from the variable stack.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface IterationSelectionManager
{
    /*
        It seems that the interface could be reduced from

            public interface IterationSelectionManager {
                Iterable<WindupVertexFrame> getFrames(GraphRewrite event, VarStack varStack);
            }

        to just

            public interface FramesSelector {
                Iterable<WindupVertexFrame> getFrames();
            }

        Because:

        1) Has not much to do with iteration
        2) the stack and the event do not necessarily have to be needed / available at the time of calling getFrames(),
        3) event is used just to get the context, which may be set in impl's constructor (or injected?),
        4) The variables stack should be accessible from the context, i.e. somehow from the event. and if not, 3) applies here too.  

        One example of this is in GremlinPipesQueryImpl which passes 
        the same event and varStack to root SelectionManager which is already there.
    */
    
    Iterable<WindupVertexFrame> getFrames(GraphRewrite event, VarStack varStack);
}
