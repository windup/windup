package org.jboss.windup.graph.test.graphservice.model;

import com.tinkerpop.blueprints.Vertex;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class AbstractModelBean implements WindupVertexFrame
{
    @Override
    public String toPrettyString() {
        try {
            return BeanUtils.describe(this).toString();
        } catch( IllegalAccessException | InvocationTargetException | NoSuchMethodException ex ) {
            return "" + this.getClass().getSimpleName();
        }
    }

    /**
     * @throws UnsupportedOperationException: Beans are not backed by graph vertices, rather java objects."
     */
    @Override
    public Vertex asVertex() {
        throw new UnsupportedOperationException("Beans are not backed by graph vertices, rather java objects.");
    }
}
