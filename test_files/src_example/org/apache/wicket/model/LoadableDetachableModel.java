package org.apache.wicket.model;

import org.apache.wicket.request.cycle.*;
import org.slf4j.*;

public abstract class LoadableDetachableModel<T> implements IModel<T>{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private transient boolean attached;
    private transient T transientModelObject;
    public LoadableDetachableModel(){
        super();
        this.attached=false;
    }
    public LoadableDetachableModel(final T object){
        super();
        this.attached=false;
        this.transientModelObject=object;
        this.attached=true;
    }
    public void detach(){
        if(this.attached){
            try{
                this.onDetach();
            }
            finally{
                this.attached=false;
                this.transientModelObject=null;
                LoadableDetachableModel.log.debug("removed transient object for {}, requestCycle {}",this,RequestCycle.get());
            }
        }
    }
    public T getObject(){
        if(!this.attached){
            this.attached=true;
            this.transientModelObject=this.load();
            if(LoadableDetachableModel.log.isDebugEnabled()){
                LoadableDetachableModel.log.debug("loaded transient object "+this.transientModelObject+" for "+this+", requestCycle "+RequestCycle.get());
            }
            this.onAttach();
        }
        return this.transientModelObject;
    }
    public final boolean isAttached(){
        return this.attached;
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder(super.toString());
        sb.append(":attached=").append(this.attached).append(":tempModelObject=[").append(this.transientModelObject).append("]");
        return sb.toString();
    }
    protected abstract T load();
    protected void onAttach(){
    }
    protected void onDetach(){
    }
    public void setObject(final T object){
        this.attached=true;
        this.transientModelObject=object;
    }
    static{
        log=LoggerFactory.getLogger(LoadableDetachableModel.class);
    }
}
