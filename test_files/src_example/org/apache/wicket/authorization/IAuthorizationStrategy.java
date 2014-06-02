package org.apache.wicket.authorization;

import org.apache.wicket.request.component.*;
import org.apache.wicket.*;

public interface IAuthorizationStrategy{
    public static final IAuthorizationStrategy ALLOW_ALL=new IAuthorizationStrategy(){
        public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> c){
            return true;
        }
        public boolean isActionAuthorized(Component c,Action action){
            return true;
        }
    };
     <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> p0);
    boolean isActionAuthorized(Component p0,Action p1);
}
