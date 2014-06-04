package org.apache.wicket.model;

import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import java.text.*;
import java.util.*;
import org.apache.wicket.util.string.interpolator.*;

public class StringResourceModel extends LoadableDetachableModel<String> implements IComponentAssignedModel<String>{
    private static final long serialVersionUID=1L;
    private final IModel<?> model;
    private final Object[] parameters;
    private final Component component;
    private final String resourceKey;
    private final String defaultValue;
    public IWrapModel<String> wrapOnAssignment(final Component component){
        return new AssignmentWrapper(component);
    }
    public StringResourceModel(final String resourceKey,final Component component,final IModel<?> model,final Object... parameters){
        this(resourceKey,component,model,null,parameters);
    }
    public StringResourceModel(final String resourceKey,final Component component,final IModel<?> model,final String defaultValue,final Object... parameters){
        super();
        if(resourceKey==null){
            throw new IllegalArgumentException("Resource key must not be null");
        }
        this.resourceKey=resourceKey;
        this.component=component;
        this.model=model;
        this.defaultValue=defaultValue;
        this.parameters=parameters;
    }
    public StringResourceModel(final String resourceKey,final IModel<?> model,final Object... parameters){
        this(resourceKey,null,model,null,parameters);
    }
    public StringResourceModel(final String resourceKey,final IModel<?> model,final String defaultValue,final Object... parameters){
        this(resourceKey,null,model,defaultValue,parameters);
    }
    public Localizer getLocalizer(){
        return Application.get().getResourceSettings().getLocalizer();
    }
    public final String getString(){
        return this.getString(this.component);
    }
    private String getString(final Component component){
        final Localizer localizer=this.getLocalizer();
        Locale locale;
        if(component!=null){
            locale=component.getLocale();
        }
        else{
            locale=(Session.exists()?Session.get().getLocale():Locale.getDefault());
        }
        final Object[] parameters=this.getParameters();
        String value;
        if(parameters==null||parameters.length==0){
            value=localizer.getString(this.getResourceKey(),component,this.model,this.defaultValue);
            if(value==null){
                value=this.defaultValue;
            }
        }
        else{
            value=localizer.getString(this.getResourceKey(),component,null,this.defaultValue);
            if(value==null){
                value=this.defaultValue;
            }
            if(value!=null){
                final Object[] realParams=new Object[parameters.length];
                for(int i=0;i<parameters.length;++i){
                    if(parameters[i] instanceof IModel){
                        realParams[i]=((IModel)parameters[i]).getObject();
                    }
                    else if(this.model!=null&&parameters[i] instanceof String){
                        realParams[i]=localizer.substitutePropertyExpressions(component,(String)parameters[i],this.model);
                    }
                    else{
                        realParams[i]=parameters[i];
                    }
                }
                if(value.indexOf(39)!=-1){
                    value=this.escapeQuotes(value);
                }
                if(this.model!=null){
                    value=Strings.replaceAll((CharSequence)value,(CharSequence)"${",(CharSequence)"$'{'").toString();
                }
                final MessageFormat format=new MessageFormat(value,locale);
                value=format.format(realParams);
                if(this.model!=null){
                    value=Strings.replaceAll((CharSequence)value,(CharSequence)"$'{'",(CharSequence)"${").toString();
                    value=localizer.substitutePropertyExpressions(component,value,this.model);
                }
            }
        }
        return value;
    }
    private String escapeQuotes(final String value){
        final StringBuilder newValue=new StringBuilder(value.length()+10);
        int count=0;
        for(int i=0;i<value.length();++i){
            final char ch=value.charAt(i);
            if(ch=='{'){
                ++count;
            }
            else if(ch=='}'){
                --count;
            }
            newValue.append(ch);
            if(ch=='\''&&count==0){
                newValue.append(ch);
            }
        }
        return newValue.toString();
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("StringResourceModel[");
        sb.append("key:");
        sb.append(this.resourceKey);
        sb.append(",default:");
        sb.append(this.defaultValue);
        sb.append(",params:");
        if(this.parameters!=null){
            sb.append(Arrays.asList(this.parameters));
        }
        sb.append("]");
        return sb.toString();
    }
    protected Object[] getParameters(){
        return this.parameters;
    }
    protected final String getResourceKey(){
        if(this.model!=null){
            return new PropertyVariableInterpolator(this.resourceKey,this.model.getObject()).toString();
        }
        return this.resourceKey;
    }
    protected String load(){
        return this.getString();
    }
    protected final void onDetach(){
        super.onDetach();
        if(this.model!=null){
            this.model.detach();
        }
        if(this.parameters!=null){
            for(final Object parameter : this.parameters){
                if(parameter instanceof IDetachable){
                    ((IDetachable)parameter).detach();
                }
            }
        }
    }
    public void setObject(final String object){
        throw new UnsupportedOperationException();
    }
    private class AssignmentWrapper extends LoadableDetachableModel<String> implements IWrapModel<String>{
        private static final long serialVersionUID=1L;
        private final Component component;
        public AssignmentWrapper(final Component component){
            super();
            this.component=component;
        }
        public void detach(){
            super.detach();
            StringResourceModel.this.detach();
        }
        protected void onDetach(){
            if(StringResourceModel.this.component==null){
                StringResourceModel.this.onDetach();
            }
        }
        protected String load(){
            if(StringResourceModel.this.component!=null){
                return StringResourceModel.this.getObject();
            }
            return StringResourceModel.this.getString(this.component);
        }
        public void setObject(final String object){
            StringResourceModel.this.setObject(object);
        }
        public IModel<String> getWrappedModel(){
            return StringResourceModel.this;
        }
    }
}
