package org.apache.wicket.markup.html.form;

import org.apache.wicket.util.string.*;
import org.apache.wicket.model.*;
import org.slf4j.*;

public abstract class AbstractTextComponent<T> extends FormComponent<T>{
    private static final int TYPE_RESOLVED=2048;
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public AbstractTextComponent(final String id){
        this(id,null);
    }
    public AbstractTextComponent(final String id,final IModel<T> model){
        super(id,model);
        this.setConvertEmptyInputStringToNull(true);
    }
    public final boolean getConvertEmptyInputStringToNull(){
        return this.getFlag(256);
    }
    public boolean isInputNullable(){
        return false;
    }
    protected void convertInput(){
        this.resolveType();
        final String[] value=this.getInputAsArray();
        final String tmp=(value!=null&&value.length>0)?value[0]:null;
        if(this.getConvertEmptyInputStringToNull()&&Strings.isEmpty((CharSequence)tmp)){
            this.setConvertedInput(null);
        }
        else{
            super.convertInput();
        }
    }
    protected void onBeforeRender(){
        super.onBeforeRender();
        this.resolveType();
    }
    private void resolveType(){
        if(!this.getFlag(2048)&&this.getType()==null){
            final Class<?> type=this.getModelType(this.getDefaultModel());
            this.setType(type);
            this.setFlag(2048,true);
        }
    }
    private Class<?> getModelType(final IModel<?> model){
        if(model instanceof IObjectClassAwareModel){
            final Class<?> objectClass=((IObjectClassAwareModel)model).getObjectClass();
            if(objectClass==null){
                AbstractTextComponent.log.warn("Couldn't resolve model type of "+model+" for "+this+", please set the type yourself.");
            }
            return objectClass;
        }
        return null;
    }
    public final FormComponent<T> setConvertEmptyInputStringToNull(final boolean flag){
        this.setFlag(256,flag);
        return this;
    }
    static{
        log=LoggerFactory.getLogger(AbstractTextComponent.class);
    }
    public interface ITextFormatProvider{
        String getTextFormat();
    }
}
