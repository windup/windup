package org.apache.wicket.util.objects.checker;

import org.apache.wicket.util.lang.*;
import java.util.*;
import org.slf4j.*;

public abstract class AbstractObjectChecker implements IObjectChecker{
    private static final Logger LOGGER;
    private final List<Class<?>> exclusions;
    protected AbstractObjectChecker(){
        this((List<Class<?>>)Generics.newArrayList());
    }
    protected AbstractObjectChecker(final List<Class<?>> exclusions){
        super();
        this.exclusions=(List<Class<?>>)Args.notNull((Object)exclusions,"exclusions");
    }
    public Result check(final Object object){
        Result result=Result.SUCCESS;
        if(object!=null&&!this.getExclusions().isEmpty()){
            final Class<?> objectType=(Class<?>)object.getClass();
            for(final Class<?> excludedType : this.getExclusions()){
                if(excludedType.isAssignableFrom(objectType)){
                    AbstractObjectChecker.LOGGER.debug("Object with type '{}' wont be checked because its type is excluded ({})",objectType,excludedType);
                    return result;
                }
            }
        }
        result=this.doCheck(object);
        return result;
    }
    protected Result doCheck(final Object object){
        return Result.SUCCESS;
    }
    public List<Class<?>> getExclusions(){
        return this.exclusions;
    }
    static{
        LOGGER=LoggerFactory.getLogger(AbstractObjectChecker.class);
    }
}
