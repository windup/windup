package org.apache.wicket.authorization.strategies;

import org.apache.wicket.request.component.*;
import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.authorization.*;

public class CompoundAuthorizationStrategy implements IAuthorizationStrategy{
    private final List<IAuthorizationStrategy> strategies;
    public CompoundAuthorizationStrategy(){
        super();
        this.strategies=(List<IAuthorizationStrategy>)new ArrayList();
    }
    public final void add(final IAuthorizationStrategy strategy){
        if(strategy==null){
            throw new IllegalArgumentException("Strategy argument cannot be null");
        }
        this.strategies.add(strategy);
    }
    public final <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> componentClass){
        for(final IAuthorizationStrategy strategy : this.strategies){
            if(!strategy.isInstantiationAuthorized(componentClass)){
                return false;
            }
        }
        return true;
    }
    public final boolean isActionAuthorized(final Component component,final Action action){
        for(final IAuthorizationStrategy strategy : this.strategies){
            if(!strategy.isActionAuthorized(component,action)){
                return false;
            }
        }
        return true;
    }
}
