package org.apache.wicket.authorization.strategies.action;

import org.apache.wicket.authorization.*;
import java.util.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.*;

public class ActionAuthorizationStrategy implements IAuthorizationStrategy{
    private final Map<Action,IActionAuthorizer> actionAuthorizerForAction;
    public ActionAuthorizationStrategy(){
        super();
        this.actionAuthorizerForAction=(Map<Action,IActionAuthorizer>)new HashMap();
    }
    public void addActionAuthorizer(final IActionAuthorizer authorizer){
        this.actionAuthorizerForAction.put(authorizer.getAction(),authorizer);
    }
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> componentClass){
        return true;
    }
    public boolean isActionAuthorized(final Component component,final Action action){
        final IActionAuthorizer authorizer=(IActionAuthorizer)this.actionAuthorizerForAction.get(action);
        return authorizer!=null&&authorizer.authorizeAction(component);
    }
}
