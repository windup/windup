package org.jboss.windup.config.gremlinquery;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jboss.windup.config.GraphRewrite;

import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.AbstractPipe;

/**
 * Converts the Windup-style {@link GremlinTransform} into something that can be used within a {@link GremlinPipeline}.
 */
class GremlinTransformAdapter<INPUT_TYPE, OUTPUT_TYPE> extends AbstractPipe<INPUT_TYPE, OUTPUT_TYPE>
{
    private final GraphRewrite event;
    private final GremlinTransform<INPUT_TYPE, OUTPUT_TYPE> transform;
    private Iterator<OUTPUT_TYPE> currentIterator;

    GremlinTransformAdapter(GraphRewrite event, GremlinTransform<INPUT_TYPE, OUTPUT_TYPE> transform)
    {
        this.event = event;
        this.transform = transform;
    }

    @SuppressWarnings("unchecked")
    private OUTPUT_TYPE getNext()
    {
        INPUT_TYPE s = (INPUT_TYPE) this.starts.next();
        OUTPUT_TYPE e = (OUTPUT_TYPE) transform.transform(event, s);
        if (e instanceof Iterable)
        {
            this.currentIterator = ((Iterable<OUTPUT_TYPE>) e).iterator();
            if (!currentIterator.hasNext())
            {
                return getNext();
            }
            return this.currentIterator.next();
        }
        else if (e instanceof Iterator)
        {
            this.currentIterator = (Iterator<OUTPUT_TYPE>) e;
            if (!currentIterator.hasNext())
            {
                return getNext();
            }
            return this.currentIterator.next();
        }
        else
        {
            currentIterator = null;
            return e;
        }
    }

    protected OUTPUT_TYPE processNextStart() throws NoSuchElementException
    {
        if (currentIterator != null && currentIterator.hasNext())
        {
            return currentIterator.next();
        }
        else
        {
            return getNext();
        }

    }
}
