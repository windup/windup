package org.apache.wicket.model.util;

import java.io.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.lang.*;

public abstract class GenericBaseModel<T> implements IModel<T>{
    private static final long serialVersionUID=1L;
    private T object;
    public T getObject(){
        return this.object;
    }
    public void setObject(T object){
        if(!(object instanceof Serializable)){
            object=this.createSerializableVersionOf(object);
        }
        this.object=object;
    }
    protected abstract T createSerializableVersionOf(final T p0);
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
        if(!(obj instanceof GenericBaseModel)){
            return false;
        }
        final GenericBaseModel<?> that=(GenericBaseModel<?>)obj;
        return Objects.equal((Object)this.object,(Object)that.object);
    }
}
