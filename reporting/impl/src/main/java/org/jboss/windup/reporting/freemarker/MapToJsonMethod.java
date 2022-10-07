package org.jboss.windup.reporting.freemarker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Gets the number of effort points involved in migrating this application.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>
 *      mapToJson(Map): String
 * </pre>
 *
 * <p> Returns a JSON-encoded object representing the given map, having the keys as property names and values as property values.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class MapToJsonMethod implements WindupFreeMarkerMethod {
    public static final Logger LOG = Logger.getLogger(MapToJsonMethod.class.getName());
    private static final String NAME = "mapToJson";

    private GraphContext graphContext;

    @Override
    public void setContext(GraphRewrite event) {
        this.graphContext = event.getGraphContext();
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Returns a JSON object representing the given map, having the keys as property names and values as property values.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);

        if (arguments.size() == 0)
            throw new TemplateModelException("Freemarker function " + NAME + "expects a Map<String, String> as a parameter.");

        final Object arg0 = arguments.get(0);
        if (null == arg0) {
            return null;
        }
        if (!(arg0 instanceof DefaultMapAdapter)) {
            LOG.warning("Expected a Freemarker's DefaultMapAdapter, was: " + arg0.getClass());
            return null;
        }

        DefaultMapAdapter mapModel = (DefaultMapAdapter) arg0;
        Map<String, String> map = (Map<String, String>) mapModel.getWrappedObject();

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(map);
            return json;
        } catch (JsonProcessingException e) {
            throw new TemplateModelException("Couldn't convert given map to a JSON.");
        } finally {
            ExecutionStatistics.get().end(NAME);
        }
    }

}
