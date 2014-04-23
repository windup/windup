package org.jboss.windup.graph.dao;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;

public interface BaseDao<T extends VertexFrame>
{
    public T create();
    public T create(Object id);
    public void delete(T obj);
    public Iterable<T> getAll();
    
    public long count(Iterable<?> obj);
    public T getById(Object id);
   
    public T castToType(Vertex vertex);
    public T castToType(VertexFrame v);
    
    public void commit();
   
   //public Iterable<T> getByProperty(String key, Object value);
   //public Iterable<T> findValueMatchingRegex(String key, String... regex);
   //public Iterable<T> hasAllProperties(String[] keys, String[] vals);
   //public TitanTransaction newTransaction();
   //public T getByUniqueProperty(String property, Object value) throws NonUniqueResultException;
}