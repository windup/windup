package org.apache.commons.lang.builder;

import java.util.Arrays;
import java.util.ArrayList;
import org.apache.commons.lang.ArrayUtils;
import java.util.Collection;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ReflectionToStringBuilder extends ToStringBuilder{
    private boolean appendStatics;
    private boolean appendTransients;
    private String[] excludeFieldNames;
    private Class upToClass;
    public static String toString(final Object object){
        return toString(object,null,false,false,null);
    }
    public static String toString(final Object object,final ToStringStyle style){
        return toString(object,style,false,false,null);
    }
    public static String toString(final Object object,final ToStringStyle style,final boolean outputTransients){
        return toString(object,style,outputTransients,false,null);
    }
    public static String toString(final Object object,final ToStringStyle style,final boolean outputTransients,final boolean outputStatics){
        return toString(object,style,outputTransients,outputStatics,null);
    }
    public static String toString(final Object object,final ToStringStyle style,final boolean outputTransients,final boolean outputStatics,final Class reflectUpToClass){
        return new ReflectionToStringBuilder(object,style,null,reflectUpToClass,outputTransients,outputStatics).toString();
    }
    public static String toString(final Object object,final ToStringStyle style,final boolean outputTransients,final Class reflectUpToClass){
        return new ReflectionToStringBuilder(object,style,null,reflectUpToClass,outputTransients).toString();
    }
    public static String toStringExclude(final Object object,final String excludeFieldName){
        return toStringExclude(object,new String[] { excludeFieldName });
    }
    public static String toStringExclude(final Object object,final Collection excludeFieldNames){
        return toStringExclude(object,toNoNullStringArray(excludeFieldNames));
    }
    static String[] toNoNullStringArray(final Collection collection){
        if(collection==null){
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return toNoNullStringArray(collection.toArray());
    }
    static String[] toNoNullStringArray(final Object[] array){
        final ArrayList list=new ArrayList(array.length);
        for(int i=0;i<array.length;++i){
            final Object e=array[i];
            if(e!=null){
                list.add(e.toString());
            }
        }
        return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
    public static String toStringExclude(final Object object,final String[] excludeFieldNames){
        return new ReflectionToStringBuilder(object).setExcludeFieldNames(excludeFieldNames).toString();
    }
    public ReflectionToStringBuilder(final Object object){
        super(object);
        this.appendStatics=false;
        this.appendTransients=false;
        this.upToClass=null;
    }
    public ReflectionToStringBuilder(final Object object,final ToStringStyle style){
        super(object,style);
        this.appendStatics=false;
        this.appendTransients=false;
        this.upToClass=null;
    }
    public ReflectionToStringBuilder(final Object object,final ToStringStyle style,final StringBuffer buffer){
        super(object,style,buffer);
        this.appendStatics=false;
        this.appendTransients=false;
        this.upToClass=null;
    }
    public ReflectionToStringBuilder(final Object object,final ToStringStyle style,final StringBuffer buffer,final Class reflectUpToClass,final boolean outputTransients){
        super(object,style,buffer);
        this.appendStatics=false;
        this.appendTransients=false;
        this.upToClass=null;
        this.setUpToClass(reflectUpToClass);
        this.setAppendTransients(outputTransients);
    }
    public ReflectionToStringBuilder(final Object object,final ToStringStyle style,final StringBuffer buffer,final Class reflectUpToClass,final boolean outputTransients,final boolean outputStatics){
        super(object,style,buffer);
        this.appendStatics=false;
        this.appendTransients=false;
        this.upToClass=null;
        this.setUpToClass(reflectUpToClass);
        this.setAppendTransients(outputTransients);
        this.setAppendStatics(outputStatics);
    }
    protected boolean accept(final Field field){
        return field.getName().indexOf(36)==-1&&(!Modifier.isTransient(field.getModifiers())||this.isAppendTransients())&&(!Modifier.isStatic(field.getModifiers())||this.isAppendStatics())&&(this.getExcludeFieldNames()==null||Arrays.binarySearch(this.getExcludeFieldNames(),field.getName())<0);
    }
    protected void appendFieldsIn(final Class clazz){
        if(clazz.isArray()){
            this.reflectionAppendArray(this.getObject());
            return;
        }
        final Field[] fields=clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields,true);
        for(int i=0;i<fields.length;++i){
            final Field field=fields[i];
            final String fieldName=field.getName();
            if(this.accept(field)){
                try{
                    final Object fieldValue=this.getValue(field);
                    this.append(fieldName,fieldValue);
                }
                catch(IllegalAccessException ex){
                    throw new InternalError("Unexpected IllegalAccessException: "+ex.getMessage());
                }
            }
        }
    }
    public String[] getExcludeFieldNames(){
        return this.excludeFieldNames;
    }
    public Class getUpToClass(){
        return this.upToClass;
    }
    protected Object getValue(final Field field) throws IllegalArgumentException,IllegalAccessException{
        return field.get(this.getObject());
    }
    public boolean isAppendStatics(){
        return this.appendStatics;
    }
    public boolean isAppendTransients(){
        return this.appendTransients;
    }
    public ToStringBuilder reflectionAppendArray(final Object array){
        this.getStyle().reflectionAppendArrayDetail(this.getStringBuffer(),null,array);
        return this;
    }
    public void setAppendStatics(final boolean appendStatics){
        this.appendStatics=appendStatics;
    }
    public void setAppendTransients(final boolean appendTransients){
        this.appendTransients=appendTransients;
    }
    public ReflectionToStringBuilder setExcludeFieldNames(final String[] excludeFieldNamesParam){
        if(excludeFieldNamesParam==null){
            this.excludeFieldNames=null;
        }
        else{
            Arrays.sort(this.excludeFieldNames=toNoNullStringArray(excludeFieldNamesParam));
        }
        return this;
    }
    public void setUpToClass(final Class clazz){
        if(clazz!=null){
            final Object object=this.getObject();
            if(object!=null&&!clazz.isInstance(object)){
                throw new IllegalArgumentException("Specified class is not a superclass of the object");
            }
        }
        this.upToClass=clazz;
    }
    public String toString(){
        if(this.getObject()==null){
            return this.getStyle().getNullText();
        }
        Class clazz=this.getObject().getClass();
        this.appendFieldsIn(clazz);
        while(clazz.getSuperclass()!=null&&clazz!=this.getUpToClass()){
            clazz=clazz.getSuperclass();
            this.appendFieldsIn(clazz);
        }
        return super.toString();
    }
}
