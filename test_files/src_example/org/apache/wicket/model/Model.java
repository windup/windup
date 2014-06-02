package org.apache.wicket.model;

import java.io.*;
import java.util.*;
import org.apache.wicket.model.util.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class Model<T extends Serializable> implements IModel<T>{
    private static final long serialVersionUID=1L;
    private T object;
    public Model(){
        super();
    }
    public Model(final T object){
        super();
        this.setObject(object);
    }
    public static <C> IModel<List<? extends C>> ofList(final List<? extends C> list){
        return (IModel<List<? extends C>>)new WildcardListModel(list);
    }
    public static <K,V> IModel<Map<K,V>> ofMap(final Map<K,V> map){
        return (IModel<Map<K,V>>)new MapModel((Map<Object,Object>)map);
    }
    public static <C> IModel<Set<? extends C>> ofSet(final Set<? extends C> set){
        return (IModel<Set<? extends C>>)new WildcardSetModel(set);
    }
    public static <C> IModel<Collection<? extends C>> of(final Collection<? extends C> set){
        return (IModel<Collection<? extends C>>)new WildcardCollectionModel(set);
    }
    public static <T extends Serializable> Model<T> of(final T object){
        return new Model<T>(object);
    }
    public static <T extends Serializable> Model<T> of(){
        return new Model<T>();
    }
    public T getObject(){
        return this.object;
    }
    public void setObject(final T object){
        if(object!=null&&!(object instanceof Serializable)){
            throw new WicketRuntimeException("Model object must be Serializable");
        }
        this.object=object;
    }
    public void detach(){
        if(this.object instanceof IDetachable){
            ((IDetachable)this.object).detach();
        }
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("Model:classname=[");
        sb.append(this.getClass().getName()).append("]");
        sb.append(":object=[").append(this.object).append("]");
        return sb.toString();
    }
    public int hashCode(){
        return Objects.hashCode(new Object[] { this.object });
    }
    public boolean equals(final Object obj){
        if(this==obj){
            return true;
        }
        if(!(obj instanceof Model)){
            return false;
        }
        final Model<?> that=(Model<?>)obj;
        return Objects.equal((Object)this.object,(Object)that.object);
    }
    public static <T> IModel<T> of(final IModel<?> model){
        return (IModel<T>)model;
    }
}
