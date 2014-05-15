package org.jboss.windup.graph.typedgraph;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeRegistry;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

public class GraphTypeManager implements TypeResolver, FrameInitializer
{
    private static final Logger LOG = LoggerFactory.getLogger(GraphTypeManager.class);

    private static final String DELIMITER = "|";

    private TypeRegistry typeRegistry = new TypeRegistry();

    public void addTypeToRegistry(Class<? extends WindupVertexFrame> wvf)
    {
        typeRegistry.add(wvf);
    }

    public void addTypeToElement(Class<? extends VertexFrame> kind, Element element)
    {
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField != null)
        {
            TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
            if (typeValueAnnotation != null)
            {
                String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
                String typeValue = typeValueAnnotation.value();
                if (typeValue.contains(DELIMITER))
                {
                    throw new IllegalArgumentException("Type value for class: " + kind.getCanonicalName() + " is "
                                + typeValue + " but this value must not contain the \"" + DELIMITER + "\" character");
                }
                else
                {
                    for (int p = 0; p < typeValue.length(); p++)
                    {
                        if (!Character.isLetterOrDigit(typeValue.charAt(p)))
                        {
                            throw new IllegalArgumentException("Type value for class: " + kind.getCanonicalName()
                                        + " is "
                                        + typeValue + " but this value must only contain numbers and letters");
                        }
                    }
                }

                // store the value in a delimited list
                String currentPropertyValue = element.getProperty(typeFieldName);
                if (currentPropertyValue == null)
                {
                    // if there is no current value, initialize with one value
                    element.setProperty(typeFieldName, DELIMITER + typeValue + DELIMITER);
                }
                else if (!currentPropertyValue.contains(DELIMITER + typeValue + DELIMITER))
                {
                    // otherwise, append to the end, making sure that the list is terminated with the delimiter
                    // character
                    element.setProperty(typeFieldName, currentPropertyValue + typeValue + DELIMITER);
                }
            }
        }
    }

    @Override
    public Class<?>[] resolveTypes(Edge e, Class<?> defaultType)
    {
        return resolve(e, defaultType);
    }

    @Override
    public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType)
    {
        return resolve(v, defaultType);
    }

    private Class<?>[] resolve(Element e, Class<?> defaultType)
    {
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(defaultType);
        if (typeHoldingTypeField != null)
        {
            String valuesAll = e.getProperty(typeHoldingTypeField.getAnnotation(TypeField.class).value());
            if (valuesAll != null)
            {
                String[] valuesArray = StringUtils.split(valuesAll, DELIMITER);
                List<Class<?>> resultClasses = new ArrayList<>();
                for (String value : valuesArray)
                {
                    Class<?> type = typeRegistry.getType(typeHoldingTypeField, value);
                    if (type != null)
                    {
                        resultClasses.add(type);
                    }
                }
                if (!resultClasses.isEmpty())
                {
                    resultClasses.add(VertexFrame.class);
                    return resultClasses.toArray(new Class<?>[resultClasses.size()]);
                }
            }
        }
        return new Class[] { defaultType, VertexFrame.class };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element)
    {
        if (VertexFrame.class.isAssignableFrom(kind))
        {
            addTypeToElement((Class<? extends VertexFrame>) kind, element);
        }
    }
}
