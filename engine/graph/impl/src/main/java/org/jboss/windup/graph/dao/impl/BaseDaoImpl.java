package org.jboss.windup.graph.dao.impl;

import java.util.Iterator;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.dao.exception.NonUniqueResultException;
import org.jboss.windup.graph.typedgraph.GraphTypeManager;

import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class BaseDaoImpl<T extends VertexFrame> implements BaseDao<T>
{

   protected final Class<T> type;
   protected final String typeValueForSearch;

   @Inject
   GraphContext context;
   
   @Inject
   private GraphTypeManager graphTypeManager;
   
   @Inject
   private GraphUtil graphUtil;

   public GraphContext getContext()
   {
      return context;
   }

   public BaseDaoImpl(Class<T> type)
   {
      this.type = type;

      TypeValue typeValue = type.getAnnotation(TypeValue.class);
      if (typeValue == null)
      {
         throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
      }
      this.typeValueForSearch = typeValue.value();
   }

   public void delete(T obj)
   {
      context.getFramed().removeVertex(obj.asVertex());
   }

   public Iterable<T> getByProperty(String key, Object value)
   {
      return context.getFramed().getVertices(key, value, type);
   }

   public Iterable<T> findValueMatchingRegex(String key, String... regex)
   {
      // build regex
      if (regex.length == 0)
      {
         return IterablesUtil.emptyIterable();
      }

      final String regexFinal;
      if (regex.length == 1)
      {
         regexFinal = regex[0];
      }
      else
      {
         StringBuilder builder = new StringBuilder();
         builder.append("\\b(");
         int i = 0;
         for (String value : regex)
         {
            if (i > 0)
            {
               builder.append("|");
            }
            builder.append(value);
            i++;
         }
         builder.append(")\\b");
         regexFinal = builder.toString();
      }

      return context.getFramed().query().has("type", Text.CONTAINS, typeValueForSearch).has(key, Text.REGEX, regexFinal).vertices(type);
   }

   public Iterable<T> hasAllProperties(String[] keys, String[] vals)
   {
      FramedGraphQuery fgq = context.getFramed().query().has("type", Text.CONTAINS, this.typeValueForSearch);

      for (int i = 0, j = keys.length; i < j; i++)
      {
         String key = keys[i];
         String val = vals[i];

         fgq = fgq.has(key, val);
      }

      return fgq.vertices(type);
   }

   public T create(Object id)
   {
      return context.getFramed().addVertex(id, type);
   }

   public T create()
   {
      return context.getFramed().addVertex(null, type);
   }

   public Iterable<T> getAll()
   {
      return context.getFramed().query().has("type", Text.CONTAINS, typeValueForSearch).vertices(type);
   }

   public TitanTransaction newTransaction()
   {
      return context.getGraph().newTransaction();
   }

   public long count(Iterable<?> obj)
   {
      GremlinPipeline pipe = new GremlinPipeline();
      return pipe.start(obj).count();
   }

   public T getById(Object id)
   {
      return context.getFramed().getVertex(id, type);
   }

   public T getByUniqueProperty(String property, Object value) throws NonUniqueResultException
   {
      Iterable<T> results = getByProperty(property, value);

      if (!results.iterator().hasNext())
      {
         return null;
      }

      Iterator<T> iter = results.iterator();
      T result = iter.next();

      if (iter.hasNext())
      {
         throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
      }

      return result;
   }

   public T castToType(VertexFrame v)
   {
      Vertex vertex = v.asVertex();
      graphTypeManager.addTypeToElement(type, vertex);
      context.getGraph().commit();
      return context.getFramed().frame(vertex, type);
   }

   public T castToType(Vertex vertex)
   {
      return graphUtil.castToType(vertex, type);
   }

   public void commit()
   {
      this.context.getGraph().commit();
   }
}