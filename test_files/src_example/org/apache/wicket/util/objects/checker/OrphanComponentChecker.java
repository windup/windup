package org.apache.wicket.util.objects.checker;

import java.util.*;
import org.apache.wicket.*;

public class OrphanComponentChecker extends AbstractObjectChecker{
    public OrphanComponentChecker(){
        super();
    }
    public OrphanComponentChecker(final List<Class<?>> exclusions){
        super(exclusions);
    }
    public IObjectChecker.Result doCheck(final Object object){
        IObjectChecker.Result result=IObjectChecker.Result.SUCCESS;
        if(object instanceof Component){
            final Component component=(Component)object;
            if(!(component instanceof Page)&&component.getParent()==null){
                result=new IObjectChecker.Result(IObjectChecker.Result.Status.FAILURE,"A component without a parent is detected.");
            }
        }
        return result;
    }
}
