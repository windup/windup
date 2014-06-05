package org.apache.wicket.util.objects.checker;

import java.util.*;
import org.apache.wicket.model.*;

public class NotDetachedModelChecker extends AbstractObjectChecker{
    public NotDetachedModelChecker(){
        super();
    }
    public NotDetachedModelChecker(final List<Class<?>> exclusions){
        super(exclusions);
    }
    public IObjectChecker.Result doCheck(final Object obj){
        IObjectChecker.Result result=IObjectChecker.Result.SUCCESS;
        if(obj instanceof LoadableDetachableModel){
            final LoadableDetachableModel<?> model=(LoadableDetachableModel<?>)obj;
            if(model.isAttached()){
                result=new IObjectChecker.Result(IObjectChecker.Result.Status.FAILURE,"Not detached model found!");
            }
        }
        return result;
    }
}
