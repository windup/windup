package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

@Singleton
public class ArchiveEntryDaoImpl extends BaseDaoImpl<ArchiveEntryResourceModel> implements ArchiveEntryDao
{

    private static Logger LOG = LoggerFactory.getLogger(ArchiveEntryDaoImpl.class);

    public ArchiveEntryDaoImpl()
    {
        super(ArchiveEntryResourceModel.class);
    }

    public Iterable<ArchiveEntryResourceModel> findByArchive(final ArchiveResourceModel resource)
    {
        Iterable<Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(context
                    .getGraph().query().has("type", Text.CONTAINS, typeValueForSearch).vertices())

                    // check to see whether there is an edge coming in that links to the resource providing the java
                    // class model.
                    .filter(new PipeFunction<Vertex, Boolean>()
                    {
                        public Boolean compute(Vertex argument)
                        {
                            Iterable<Vertex> v = argument.getVertices(Direction.IN, "child");
                            return v.iterator().next().getId().equals(resource.asVertex().getId());
                        }
                    });
        return context.getFramed().frameVertices(pipeline, ArchiveEntryResourceModel.class);
    }

    public Iterable<ArchiveEntryResourceModel> findArchiveEntry(String value)
    {
        return super.getByProperty("archiveEntry", value);
    }

    public long findArchiveEntryWithExtensionCount(String... values)
    {
        return count(findArchiveEntryWithExtension(values));
    }

    public Iterable<ArchiveEntryResourceModel> findArchiveEntryWithExtension(String... values)
    {
        // build regex
        if (values.length == 0)
        {
            return IterablesUtil.emptyIterable();
        }

        final String regex;
        if (values.length == 1)
        {
            regex = ".+\\." + values[0] + "$";
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b(");
            int i = 0;
            for (String value : values)
            {
                if (i > 0)
                {
                    builder.append("|");
                }
                builder.append(value);
                i++;
            }
            builder.append(")\\b");
            regex = ".+\\." + builder.toString() + "$";
        }

        LOG.debug("Regex: " + regex);
        return context.getFramed().query().has("type", Text.CONTAINS, typeValueForSearch).has("archiveEntry", Text.REGEX, regex).vertices(type);
    }
}
