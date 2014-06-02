package org.apache.wicket;

import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.behavior.*;

final class Behaviors implements IDetachable{
    private static final long serialVersionUID=1L;
    private final Component component;
    public Behaviors(final Component component){
        super();
        this.component=component;
    }
    public void add(final Behavior... behaviors){
        if(behaviors==null){
            throw new IllegalArgumentException("Argument may not be null");
        }
        for(final Behavior behavior : behaviors){
            if(behavior==null){
                throw new IllegalArgumentException("Argument may not be null");
            }
            this.internalAdd(behavior);
            if(!behavior.isTemporary(this.component)){
                this.component.addStateChange();
            }
            behavior.bind(this.component);
        }
    }
    private void internalAdd(final Behavior behavior){
        this.component.data_add(behavior);
        if(behavior.getStatelessHint(this.component)){
            this.getBehaviorId(behavior);
        }
    }
    public <M extends Behavior> List<M> getBehaviors(final Class<M> type){
        final int len=this.component.data_length();
        final int start=this.component.data_start();
        if(len<start){
            return (List<M>)Collections.emptyList();
        }
        final List<M> subset=(List<M>)new ArrayList(len);
        for(int i=this.component.data_start();i<len;++i){
            final Object obj=this.component.data_get(i);
            if(obj!=null&&obj instanceof Behavior&&(type==null||type.isAssignableFrom(obj.getClass()))){
                subset.add(obj);
            }
        }
        return (List<M>)Collections.unmodifiableList(subset);
    }
    public void remove(final Behavior behavior){
        if(behavior==null){
            throw new IllegalArgumentException("Argument `behavior` cannot be null");
        }
        if(this.internalRemove(behavior)){
            if(!behavior.isTemporary(this.component)){
                this.component.addStateChange();
            }
            behavior.detach(this.component);
            return;
        }
        throw new IllegalStateException("Tried to remove a behavior that was not added to the component. Behavior: "+behavior.toString());
    }
    public final void detach(){
        for(int len=this.component.data_length(),i=this.component.data_start();i<len;++i){
            final Object obj=this.component.data_get(i);
            if(obj!=null&&obj instanceof Behavior){
                final Behavior behavior=(Behavior)obj;
                behavior.detach(this.component);
                if(behavior.isTemporary(this.component)){
                    this.internalRemove(behavior);
                }
            }
        }
    }
    private boolean internalRemove(final Behavior behavior){
        for(int len=this.component.data_length(),i=this.component.data_start();i<len;++i){
            final Object o=this.component.data_get(i);
            if(o!=null&&o.equals(behavior)){
                this.component.data_remove(i);
                behavior.unbind(this.component);
                final ArrayList<Behavior> ids=this.getBehaviorsIdList(false);
                if(ids!=null){
                    final int idx=ids.indexOf(behavior);
                    if(idx==ids.size()-1){
                        ids.remove(idx);
                    }
                    else if(idx>=0){
                        ids.set(idx,null);
                    }
                    ids.trimToSize();
                    if(ids.isEmpty()){
                        this.removeBehaviorsIdList();
                    }
                }
                return true;
            }
        }
        return false;
    }
    private void removeBehaviorsIdList(){
        for(int i=this.component.data_start();i<this.component.data_length();++i){
            final Object obj=this.component.data_get(i);
            if(obj!=null&&obj instanceof BehaviorIdList){
                this.component.data_remove(i);
                return;
            }
        }
    }
    private BehaviorIdList getBehaviorsIdList(final boolean createIfNotFound){
        for(int len=this.component.data_length(),i=this.component.data_start();i<len;++i){
            final Object obj=this.component.data_get(i);
            if(obj!=null&&obj instanceof BehaviorIdList){
                return (BehaviorIdList)obj;
            }
        }
        if(createIfNotFound){
            final BehaviorIdList list=new BehaviorIdList();
            this.component.data_add(list);
            return list;
        }
        return null;
    }
    public final int getBehaviorId(final Behavior behavior){
        Args.notNull((Object)behavior,"behavior");
        boolean found=false;
        for(int i=this.component.data_start();i<this.component.data_length();++i){
            if(behavior==this.component.data_get(i)){
                found=true;
                break;
            }
        }
        if(!found){
            throw new IllegalStateException("Behavior must be added to component before its id can be generated. Behavior: "+behavior+", Component: "+this);
        }
        final ArrayList<Behavior> ids=this.getBehaviorsIdList(true);
        int id=ids.indexOf(behavior);
        if(id<0){
            for(int j=0;j<ids.size();++j){
                if(ids.get(j)==null){
                    ids.set(j,behavior);
                    id=j;
                    break;
                }
            }
        }
        if(id<0){
            id=ids.size();
            ids.add(behavior);
            ids.trimToSize();
        }
        return id;
    }
    public final Behavior getBehaviorById(final int id){
        Behavior behavior=null;
        final ArrayList<Behavior> ids=this.getBehaviorsIdList(false);
        if(ids!=null&&id>=0&&id<ids.size()){
            behavior=(Behavior)ids.get(id);
        }
        if(behavior!=null){
            return behavior;
        }
        throw new InvalidBehaviorIdException(this.component,id);
    }
    private static class BehaviorIdList extends ArrayList<Behavior>{
        private static final long serialVersionUID=1L;
        public BehaviorIdList(){
            super(1);
        }
    }
}
