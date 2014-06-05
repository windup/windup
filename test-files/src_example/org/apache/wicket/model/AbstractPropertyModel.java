package org.apache.wicket.model;

import org.apache.wicket.util.string.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import java.lang.reflect.*;
import org.slf4j.*;

public abstract class AbstractPropertyModel<T> implements IChainingModel<T>,IObjectClassAwareModel<T>,IPropertyReflectionAwareModel<T>{
    private static final Logger logger;
    private static final long serialVersionUID=1L;
    private Object target;
    public AbstractPropertyModel(final Object modelObject){
        super();
        if(modelObject==null){
            throw new IllegalArgumentException("Parameter modelObject cannot be null");
        }
        if(modelObject instanceof Session){
            AbstractPropertyModel.logger.warn("It is not a good idea to reference the Session instance in models directly as it may lead to serialization problems. If you need to access a property of the session via the model use the page instance as the model object and 'session.attribute' as the path.");
        }
        this.target=modelObject;
    }
    public void detach(){
        if(this.target instanceof IDetachable){
            ((IDetachable)this.target).detach();
        }
    }
    public IModel<?> getChainedModel(){
        if(this.target instanceof IModel){
            return (IModel<?>)this.target;
        }
        return null;
    }
    public T getObject(){
        final String expression=this.propertyExpression();
        if(Strings.isEmpty((CharSequence)expression)){
            return (T)this.getTarget();
        }
        if(expression.startsWith(".")){
            throw new IllegalArgumentException("Property expressions cannot start with a '.' character");
        }
        final Object target=this.getTarget();
        if(target!=null){
            return (T)PropertyResolver.getValue(expression,target);
        }
        return null;
    }
    public final String getPropertyExpression(){
        return this.propertyExpression();
    }
    public void setChainedModel(final IModel<?> model){
        this.target=model;
    }
    public void setObject(final T object){
        final String expression=this.propertyExpression();
        if(Strings.isEmpty((CharSequence)expression)){
            if(this.target instanceof IModel){
                ((IModel)this.target).setObject(object);
            }
            else{
                this.target=object;
            }
        }
        else{
            PropertyResolverConverter prc=null;
            prc=new PropertyResolverConverter(Application.get().getConverterLocator(),Session.get().getLocale());
            PropertyResolver.setValue(expression,this.getTarget(),object,prc);
        }
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("Model:classname=[");
        sb.append(this.getClass().getName()).append("]");
        sb.append(":nestedModel=[").append(this.target).append("]");
        return sb.toString();
    }
    public final Object getTarget(){
        Object object;
        Object tmp;
        for(object=this.target;object instanceof IModel;object=tmp){
            tmp=((IModel)object).getObject();
            if(tmp==object){
                break;
            }
        }
        return object;
    }
    public Class<T> getObjectClass(){
        final String expression=this.propertyExpression();
        if(Strings.isEmpty((CharSequence)expression)){
            final Object target=this.getTarget();
            return (Class<T>)((target!=null)?target.getClass():null);
        }
        final Object target=this.getTarget();
        if(target!=null){
            try{
                return (Class<T>)PropertyResolver.getPropertyClass(expression,target);
            }
            catch(Exception e){
                return null;
            }
        }
        if(this.target instanceof IObjectClassAwareModel){
            try{
                final Class<?> targetClass=((IObjectClassAwareModel)this.target).getObjectClass();
                if(targetClass!=null){
                    return PropertyResolver.getPropertyClass(expression,targetClass);
                }
            }
            catch(WicketRuntimeException ex){
            }
        }
        return null;
    }
    public Field getPropertyField(){
        final String expression=this.propertyExpression();
        if(!Strings.isEmpty((CharSequence)expression)){
            final Object target=this.getTarget();
            if(target!=null){
                try{
                    return PropertyResolver.getPropertyField(expression,target);
                }
                catch(Exception ex){
                }
            }
        }
        return null;
    }
    public Method getPropertyGetter(){
        final String expression=this.propertyExpression();
        if(!Strings.isEmpty((CharSequence)expression)){
            final Object target=this.getTarget();
            if(target!=null){
                try{
                    return PropertyResolver.getPropertyGetter(expression,target);
                }
                catch(Exception ex){
                }
            }
        }
        return null;
    }
    public Method getPropertySetter(){
        final String expression=this.propertyExpression();
        if(!Strings.isEmpty((CharSequence)expression)){
            final Object target=this.getTarget();
            if(target!=null){
                try{
                    return PropertyResolver.getPropertySetter(expression,target);
                }
                catch(Exception ex){
                }
            }
        }
        return null;
    }
    protected abstract String propertyExpression();
    static{
        logger=LoggerFactory.getLogger(AbstractPropertyModel.class);
    }
}
