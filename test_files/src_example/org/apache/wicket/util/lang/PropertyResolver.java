package org.apache.wicket.util.lang;

import java.util.concurrent.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import org.slf4j.*;
import java.lang.reflect.*;
import org.apache.wicket.util.convert.*;

public final class PropertyResolver{
    private static final Logger log;
    private static final int RETURN_NULL=0;
    private static final int CREATE_NEW_VALUE=1;
    private static final int RESOLVE_CLASS=2;
    private static final ConcurrentHashMap<Object,IClassCache> applicationToClassesToGetAndSetters;
    private static final String GET="get";
    private static final String IS="is";
    private static final String SET="set";
    public static final Object getValue(final String expression,final Object object){
        if(expression==null||expression.equals("")||object==null){
            return object;
        }
        final ObjectAndGetSetter getter=getObjectAndGetSetter(expression,object,0);
        if(getter==null){
            return null;
        }
        return getter.getValue();
    }
    public static final void setValue(final String expression,final Object object,final Object value,final PropertyResolverConverter converter){
        if(expression==null||expression.equals("")){
            throw new WicketRuntimeException("Empty expression setting value: "+value+" on object: "+object);
        }
        if(object==null){
            throw new WicketRuntimeException("Attempted to set property value on a null object. Property expression: "+expression+" Value: "+value);
        }
        final ObjectAndGetSetter setter=getObjectAndGetSetter(expression,object,1);
        if(setter==null){
            throw new WicketRuntimeException("Null object returned for expression: "+expression+" for setting value: "+value+" on: "+object);
        }
        setter.setValue(value,(converter==null)?new PropertyResolverConverter(Application.get().getConverterLocator(),Session.get().getLocale()):converter);
    }
    public static final Class<?> getPropertyClass(final String expression,final Object object){
        final ObjectAndGetSetter setter=getObjectAndGetSetter(expression,object,2);
        if(setter==null){
            throw new WicketRuntimeException("Null object returned for expression: "+expression+" for getting the target classs of: "+object);
        }
        return setter.getTargetClass();
    }
    public static <T> Class<T> getPropertyClass(final String expression,final Class<?> clz){
        final ObjectAndGetSetter setter=getObjectAndGetSetter(expression,null,2,clz);
        if(setter==null){
            throw new WicketRuntimeException("No Class returned for expression: "+expression+" for getting the target classs of: "+clz);
        }
        return (Class<T>)setter.getTargetClass();
    }
    public static final Field getPropertyField(final String expression,final Object object){
        final ObjectAndGetSetter setter=getObjectAndGetSetter(expression,object,2);
        if(setter==null){
            throw new WicketRuntimeException("Null object returned for expression: "+expression+" for getting the target classs of: "+object);
        }
        return setter.getField();
    }
    public static final Method getPropertyGetter(final String expression,final Object object){
        final ObjectAndGetSetter setter=getObjectAndGetSetter(expression,object,2);
        if(setter==null){
            throw new WicketRuntimeException("Null object returned for expression: "+expression+" for getting the target classs of: "+object);
        }
        return setter.getGetter();
    }
    public static final Method getPropertySetter(final String expression,final Object object){
        final ObjectAndGetSetter setter=getObjectAndGetSetter(expression,object,2);
        if(setter==null){
            throw new WicketRuntimeException("Null object returned for expression: "+expression+" for getting the target classs of: "+object);
        }
        return setter.getSetter();
    }
    private static ObjectAndGetSetter getObjectAndGetSetter(final String expression,final Object object,final int tryToCreateNull){
        return getObjectAndGetSetter(expression,object,tryToCreateNull,(Class<?>)object.getClass());
    }
    private static ObjectAndGetSetter getObjectAndGetSetter(final String expression,final Object object,final int tryToCreateNull,Class<?> clz){
        String expressionBracketsSeperated;
        int index;
        for(expressionBracketsSeperated=Strings.replaceAll((CharSequence)expression,(CharSequence)"[",(CharSequence)".[").toString(),index=getNextDotIndex(expressionBracketsSeperated,0);index==0&&expressionBracketsSeperated.startsWith(".");expressionBracketsSeperated=expressionBracketsSeperated.substring(1),index=getNextDotIndex(expressionBracketsSeperated,0)){
        }
        int lastIndex=0;
        Object value=object;
        String exp=expressionBracketsSeperated;
        while(index!=-1){
            exp=expressionBracketsSeperated.substring(lastIndex,index);
            if(exp.length()==0){
                exp=expressionBracketsSeperated.substring(index+1);
                break;
            }
            IGetAndSet getAndSetter=null;
            try{
                getAndSetter=getGetAndSetter(exp,clz);
            }
            catch(WicketRuntimeException ex){
                index=getNextDotIndex(expressionBracketsSeperated,index+1);
                if(index==-1){
                    exp=expressionBracketsSeperated.substring(lastIndex);
                    break;
                }
                final String indexExpression=expressionBracketsSeperated.substring(lastIndex,index);
                getAndSetter=getGetAndSetter(indexExpression,clz);
            }
            Object newValue=null;
            if(value!=null){
                newValue=getAndSetter.getValue(value);
            }
            if(newValue==null){
                if(tryToCreateNull==1){
                    newValue=getAndSetter.newValue(value);
                    if(newValue==null){
                        return null;
                    }
                }
                else{
                    if(tryToCreateNull!=2){
                        return null;
                    }
                    clz=getAndSetter.getTargetClass();
                }
            }
            value=newValue;
            if(value!=null){
                clz=(Class<?>)value.getClass();
            }
            lastIndex=index+1;
            index=getNextDotIndex(expressionBracketsSeperated,lastIndex);
            if(index==-1){
                exp=expressionBracketsSeperated.substring(lastIndex);
                break;
            }
        }
        IGetAndSet getAndSetter=getGetAndSetter(exp,clz);
        return new ObjectAndGetSetter(getAndSetter,value);
    }
    private static int getNextDotIndex(final String expression,final int start){
        boolean insideBracket=false;
        for(int i=start;i<expression.length();++i){
            final char ch=expression.charAt(i);
            if(ch=='.'&&!insideBracket){
                return i;
            }
            if(ch=='['){
                insideBracket=true;
            }
            else if(ch==']'){
                insideBracket=false;
            }
        }
        return -1;
    }
    private static final IGetAndSet getGetAndSetter(String exp,final Class<?> clz){
        final IClassCache classesToGetAndSetters=getClassesToGetAndSetters();
        Map<String,IGetAndSet> getAndSetters=classesToGetAndSetters.get(clz);
        if(getAndSetters==null){
            getAndSetters=(Map<String,IGetAndSet>)new ConcurrentHashMap(8);
            classesToGetAndSetters.put(clz,getAndSetters);
        }
        IGetAndSet getAndSetter=(IGetAndSet)getAndSetters.get(exp);
        if(getAndSetter==null){
            Method method=null;
            Field field=null;
            if(exp.startsWith("[")){
                exp=exp.substring(1,exp.length()-1);
            }
            else if(exp.endsWith("()")){
                method=findMethod(clz,exp);
            }
            else{
                method=findGetter(clz,exp);
            }
            if(method==null){
                if(List.class.isAssignableFrom(clz)){
                    try{
                        final int index=Integer.parseInt(exp);
                        getAndSetter=new ListGetSet(index);
                    }
                    catch(NumberFormatException ex){
                        method=findMethod(clz,exp);
                        if(method!=null){
                            getAndSetter=new MethodGetAndSet(method,MethodGetAndSet.access$000(method,clz),null);
                        }
                        else{
                            field=findField(clz,exp);
                            if(field==null){
                                throw new WicketRuntimeException("The expression '"+exp+"' is neither an index nor is it a method or field for the list "+clz);
                            }
                            getAndSetter=new FieldGetAndSetter(field);
                        }
                    }
                }
                else if(Map.class.isAssignableFrom(clz)){
                    getAndSetter=new MapGetSet(exp);
                }
                else if(clz.isArray()){
                    try{
                        final int index=Integer.parseInt(exp);
                        getAndSetter=new ArrayGetSet((Class<?>)clz.getComponentType(),index);
                    }
                    catch(NumberFormatException ex){
                        if(!exp.equals("length")&&!exp.equals("size")){
                            throw new WicketRuntimeException("Can't parse the expression '"+exp+"' as an index for an array lookup");
                        }
                        getAndSetter=new ArrayLengthGetSet();
                    }
                }
                else{
                    field=findField(clz,exp);
                    if(field==null){
                        method=findMethod(clz,exp);
                        if(method==null){
                            final int index=exp.indexOf(46);
                            if(index==-1){
                                throw new WicketRuntimeException("No get method defined for class: "+clz+" expression: "+exp);
                            }
                            final String propertyName=exp.substring(0,index);
                            final String propertyIndex=exp.substring(index+1);
                            try{
                                final int parsedIndex=Integer.parseInt(propertyIndex);
                                final String name=Character.toUpperCase(propertyName.charAt(0))+propertyName.substring(1);
                                method=clz.getMethod("get"+name,new Class[] { Integer.TYPE });
                                getAndSetter=new ArrayPropertyGetSet(method,parsedIndex);
                            }
                            catch(Exception e){
                                throw new WicketRuntimeException("No get method defined for class: "+clz+" expression: "+propertyName);
                            }
                        }
                        else{
                            getAndSetter=new MethodGetAndSet(method,MethodGetAndSet.access$000(method,clz),null);
                        }
                    }
                    else{
                        getAndSetter=new FieldGetAndSetter(field);
                    }
                }
            }
            else{
                field=findField(clz,exp);
                getAndSetter=new MethodGetAndSet(method,MethodGetAndSet.access$000(method,clz),field);
            }
            getAndSetters.put(exp,getAndSetter);
        }
        return getAndSetter;
    }
    private static Field findField(final Class<?> clz,final String expression){
        Field field=null;
        try{
            field=clz.getField(expression);
        }
        catch(Exception e){
            for(Class<?> tmp=clz;tmp!=null&&tmp!=Object.class;tmp=(Class<?>)tmp.getSuperclass()){
                final Field[] arr$;
                final Field[] fields=arr$=tmp.getDeclaredFields();
                for(final Field aField : arr$){
                    if(aField.getName().equals(expression)){
                        aField.setAccessible(true);
                        return aField;
                    }
                }
            }
            PropertyResolver.log.debug("Cannot find field "+clz+"."+expression);
        }
        return field;
    }
    private static final Method findGetter(final Class<?> clz,final String expression){
        final String name=Character.toUpperCase(expression.charAt(0))+expression.substring(1);
        Method method=null;
        try{
            method=clz.getMethod("get"+name,null);
        }
        catch(Exception ex){
        }
        if(method==null){
            try{
                method=clz.getMethod("is"+name,null);
            }
            catch(Exception e){
                PropertyResolver.log.debug("Cannot find getter "+clz+"."+expression);
            }
        }
        return method;
    }
    private static final Method findMethod(final Class<?> clz,String expression){
        if(expression.endsWith("()")){
            expression=expression.substring(0,expression.length()-2);
        }
        Method method=null;
        try{
            method=clz.getMethod(expression,null);
        }
        catch(Exception e){
            PropertyResolver.log.debug("Cannot find method "+clz+"."+expression);
        }
        return method;
    }
    private static IClassCache getClassesToGetAndSetters(){
        Object key;
        if(Application.exists()){
            key=Application.get();
        }
        else{
            key=PropertyResolver.class;
        }
        IClassCache result=PropertyResolver.applicationToClassesToGetAndSetters.get(key);
        if(result==null){
            final IClassCache tmpResult=PropertyResolver.applicationToClassesToGetAndSetters.putIfAbsent(key,result=new DefaultClassCache());
            if(tmpResult!=null){
                result=tmpResult;
            }
        }
        return result;
    }
    public static void destroy(final Application application){
        PropertyResolver.applicationToClassesToGetAndSetters.remove(application);
    }
    public static void setClassCache(final Application application,final IClassCache classCache){
        if(application!=null){
            PropertyResolver.applicationToClassesToGetAndSetters.put(application,classCache);
        }
        else{
            PropertyResolver.applicationToClassesToGetAndSetters.put(PropertyResolver.class,classCache);
        }
    }
    static{
        log=LoggerFactory.getLogger(PropertyResolver.class);
        applicationToClassesToGetAndSetters=Generics.newConcurrentHashMap(2);
    }
    private static final class ObjectAndGetSetter{
        private final IGetAndSet getAndSetter;
        private final Object value;
        public ObjectAndGetSetter(final IGetAndSet getAndSetter,final Object value){
            super();
            this.getAndSetter=getAndSetter;
            this.value=value;
        }
        public void setValue(final Object value,final PropertyResolverConverter converter){
            this.getAndSetter.setValue(this.value,value,converter);
        }
        public Object getValue(){
            return this.getAndSetter.getValue(this.value);
        }
        public Class<?> getTargetClass(){
            return this.getAndSetter.getTargetClass();
        }
        public Field getField(){
            return this.getAndSetter.getField();
        }
        public Method getGetter(){
            return this.getAndSetter.getGetter();
        }
        public Method getSetter(){
            return this.getAndSetter.getSetter();
        }
    }
    private abstract static class AbstractGetAndSet implements IGetAndSet{
        public Field getField(){
            return null;
        }
        public Method getGetter(){
            return null;
        }
        public Method getSetter(){
            return null;
        }
        public Class<?> getTargetClass(){
            return null;
        }
    }
    private static final class MapGetSet extends AbstractGetAndSet{
        private final String key;
        MapGetSet(final String key){
            super();
            this.key=key;
        }
        public Object getValue(final Object object){
            return ((Map)object).get(this.key);
        }
        public void setValue(final Object object,final Object value,final PropertyResolverConverter converter){
            ((Map)object).put(this.key,value);
        }
        public Object newValue(final Object object){
            return null;
        }
    }
    private static final class ListGetSet extends AbstractGetAndSet{
        private final int index;
        ListGetSet(final int index){
            super();
            this.index=index;
        }
        public Object getValue(final Object object){
            if(((List)object).size()<=this.index){
                return null;
            }
            return ((List)object).get(this.index);
        }
        public void setValue(final Object object,final Object value,final PropertyResolverConverter converter){
            final List<Object> lst=(List<Object>)object;
            if(lst.size()>this.index){
                lst.set(this.index,value);
            }
            else if(lst.size()==this.index){
                lst.add(value);
            }
            else{
                while(lst.size()<this.index){
                    lst.add(null);
                }
                lst.add(value);
            }
        }
        public Object newValue(final Object object){
            return null;
        }
    }
    private static final class ArrayGetSet extends AbstractGetAndSet{
        private final int index;
        private final Class<?> clzComponentType;
        ArrayGetSet(final Class<?> clzComponentType,final int index){
            super();
            this.clzComponentType=clzComponentType;
            this.index=index;
        }
        public Object getValue(final Object object){
            if(Array.getLength(object)>this.index){
                return Array.get(object,this.index);
            }
            return null;
        }
        public void setValue(final Object object,Object value,final PropertyResolverConverter converter){
            value=converter.convert(value,this.clzComponentType);
            Array.set(object,this.index,value);
        }
        public Object newValue(final Object object){
            Object value=null;
            try{
                value=this.clzComponentType.newInstance();
                Array.set(object,this.index,value);
            }
            catch(Exception e){
                PropertyResolver.log.warn("Cannot set new value "+value+" at index "+this.index+" for array holding elements of class "+this.clzComponentType,e);
            }
            return value;
        }
        public Class<?> getTargetClass(){
            return this.clzComponentType;
        }
    }
    private static final class ArrayLengthGetSet extends AbstractGetAndSet{
        public Object getValue(final Object object){
            return Array.getLength(object);
        }
        public void setValue(final Object object,final Object value,final PropertyResolverConverter converter){
            throw new WicketRuntimeException("You can't set the length on an array:"+object);
        }
        public Object newValue(final Object object){
            throw new WicketRuntimeException("Can't get a new value from a length of an array: "+object);
        }
        public Class<?> getTargetClass(){
            return (Class<?>)Integer.TYPE;
        }
    }
    private static final class ArrayPropertyGetSet extends AbstractGetAndSet{
        private final Integer index;
        private final Method getMethod;
        private Method setMethod;
        ArrayPropertyGetSet(final Method method,final int index){
            super();
            this.index=index;
            (this.getMethod=method).setAccessible(true);
        }
        private static final Method findSetter(final Method getMethod,final Class<?> clz){
            String name=getMethod.getName();
            name="set"+name.substring(3);
            try{
                return clz.getMethod(name,new Class[] { Integer.TYPE,getMethod.getReturnType() });
            }
            catch(Exception e){
                PropertyResolver.log.debug("Can't find setter method corresponding to "+getMethod);
                return null;
            }
        }
        public Object getValue(final Object object){
            Object ret=null;
            try{
                ret=this.getMethod.invoke(object,new Object[] { this.index });
            }
            catch(InvocationTargetException ex){
                throw new WicketRuntimeException("Error calling index property method: "+this.getMethod+" on object: "+object,ex.getCause());
            }
            catch(Exception ex2){
                throw new WicketRuntimeException("Error calling index property method: "+this.getMethod+" on object: "+object,ex2);
            }
            return ret;
        }
        public void setValue(final Object object,final Object value,final PropertyResolverConverter converter){
            if(this.setMethod==null){
                this.setMethod=findSetter(this.getMethod,(Class<?>)object.getClass());
            }
            if(this.setMethod==null){
                throw new WicketRuntimeException("No set method defined for value: "+value+" on object: "+object);
            }
            this.setMethod.setAccessible(true);
            final Object converted=converter.convert(value,(Class<Object>)this.getMethod.getReturnType());
            if(converted==null&&value!=null){
                throw new ConversionException("Can't convert value: "+value+" to class: "+this.getMethod.getReturnType()+" for setting it on "+object);
            }
            try{
                this.setMethod.invoke(object,new Object[] { this.index,converted });
            }
            catch(InvocationTargetException ex){
                throw new WicketRuntimeException("Error index property calling method: "+this.setMethod+" on object: "+object,ex.getCause());
            }
            catch(Exception ex2){
                throw new WicketRuntimeException("Error index property calling method: "+this.setMethod+" on object: "+object,ex2);
            }
        }
        public Class<?> getTargetClass(){
            return (Class<?>)this.getMethod.getReturnType();
        }
        public Object newValue(final Object object){
            if(this.setMethod==null){
                this.setMethod=findSetter(this.getMethod,(Class<?>)object.getClass());
            }
            if(this.setMethod==null){
                PropertyResolver.log.warn("Null setMethod");
                return null;
            }
            final Class<?> clz=(Class<?>)this.getMethod.getReturnType();
            Object value=null;
            try{
                value=clz.newInstance();
                this.setMethod.invoke(object,new Object[] { this.index,value });
            }
            catch(Exception e){
                PropertyResolver.log.warn("Cannot set new value "+value+" at index "+this.index,e);
            }
            return value;
        }
    }
    private static final class MethodGetAndSet extends AbstractGetAndSet{
        private final Method getMethod;
        private final Method setMethod;
        private final Field field;
        MethodGetAndSet(final Method getMethod,final Method setMethod,final Field field){
            super();
            (this.getMethod=getMethod).setAccessible(true);
            this.field=field;
            this.setMethod=setMethod;
        }
        public final Object getValue(final Object object){
            Object ret=null;
            try{
                ret=this.getMethod.invoke(object,null);
            }
            catch(InvocationTargetException ex){
                throw new WicketRuntimeException("Error calling method: "+this.getMethod+" on object: "+object,ex.getCause());
            }
            catch(Exception ex2){
                throw new WicketRuntimeException("Error calling method: "+this.getMethod+" on object: "+object,ex2);
            }
            return ret;
        }
        public final void setValue(final Object object,final Object value,final PropertyResolverConverter converter){
            Class<?> type=null;
            if(this.setMethod!=null){
                type=(Class<?>)this.getMethod.getReturnType();
            }
            else if(this.field!=null){
                type=(Class<?>)this.field.getType();
            }
            Object converted=null;
            if(type!=null){
                converted=converter.convert(value,type);
                if(converted==null){
                    if(value!=null){
                        throw new ConversionException("Method ["+this.getMethod+"]. Can't convert value: "+value+" to class: "+this.getMethod.getReturnType()+" for setting it on "+object);
                    }
                    if(this.getMethod.getReturnType().isPrimitive()){
                        throw new ConversionException("Method ["+this.getMethod+"]. Can't convert null value to a primitive class: "+this.getMethod.getReturnType()+" for setting it on "+object);
                    }
                }
            }
            if(this.setMethod!=null){
                try{
                    this.setMethod.invoke(object,new Object[] { converted });
                    return;
                }
                catch(InvocationTargetException ex){
                    throw new WicketRuntimeException("Error calling method: "+this.setMethod+" on object: "+object,ex.getCause());
                }
                catch(Exception ex2){
                    throw new WicketRuntimeException("Error calling method: "+this.setMethod+" on object: "+object,ex2);
                }
            }
            if(this.field!=null){
                try{
                    this.field.set(object,converted);
                    return;
                }
                catch(Exception ex2){
                    throw new WicketRuntimeException("Error setting field: "+this.field+" on object: "+object,ex2);
                }
            }
            throw new WicketRuntimeException("no set method defined for value: "+value+" on object: "+object+" while respective getMethod being "+this.getMethod.getName());
        }
        private static final Method findSetter(final Method getMethod,final Class<?> clz){
            String name=getMethod.getName();
            if(name.startsWith("get")){
                name="set"+name.substring(3);
            }
            else{
                name="set"+name.substring(2);
            }
            try{
                final Method method=clz.getMethod(name,new Class[] { getMethod.getReturnType() });
                if(method!=null){
                    method.setAccessible(true);
                }
                return method;
            }
            catch(NoSuchMethodException e){
                final Method[] arr$;
                final Method[] methods=arr$=clz.getMethods();
                for(final Method method2 : arr$){
                    if(method2.getName().equals(name)){
                        final Class<?>[] parameterTypes=(Class<?>[])method2.getParameterTypes();
                        if(parameterTypes.length==1&&parameterTypes[0].isAssignableFrom(getMethod.getReturnType())){
                            return method2;
                        }
                    }
                }
                PropertyResolver.log.debug("Cannot find setter corresponding to "+getMethod);
            }
            catch(Exception e2){
                PropertyResolver.log.debug("Cannot find setter corresponding to "+getMethod);
            }
            return null;
        }
        public Object newValue(final Object object){
            if(this.setMethod==null){
                PropertyResolver.log.warn("Null setMethod");
                return null;
            }
            final Class<?> clz=(Class<?>)this.getMethod.getReturnType();
            Object value=null;
            try{
                value=clz.newInstance();
                this.setMethod.invoke(object,new Object[] { value });
            }
            catch(Exception e){
                PropertyResolver.log.warn("Cannot set new value "+value,e);
            }
            return value;
        }
        public Class<?> getTargetClass(){
            return (Class<?>)this.getMethod.getReturnType();
        }
        public Method getGetter(){
            return this.getMethod;
        }
        public Method getSetter(){
            return this.setMethod;
        }
        public Field getField(){
            return this.field;
        }
        static /* synthetic */ Method access$000(final Method x0,final Class x1){
            return findSetter(x0,(Class<?>)x1);
        }
    }
    private static class FieldGetAndSetter extends AbstractGetAndSet{
        private final Field field;
        public FieldGetAndSetter(final Field field){
            super();
            (this.field=field).setAccessible(true);
        }
        public Object getValue(final Object object){
            try{
                return this.field.get(object);
            }
            catch(Exception ex){
                throw new WicketRuntimeException("Error getting field value of field "+this.field+" from object "+object,ex);
            }
        }
        public Object newValue(final Object object){
            final Class<?> clz=(Class<?>)this.field.getType();
            Object value=null;
            try{
                value=clz.newInstance();
                this.field.set(object,value);
            }
            catch(Exception e){
                PropertyResolver.log.warn("Cannot set field "+this.field+" to "+value,e);
            }
            return value;
        }
        public void setValue(final Object object,Object value,final PropertyResolverConverter converter){
            value=converter.convert(value,(Class<Object>)this.field.getType());
            try{
                this.field.set(object,value);
            }
            catch(Exception ex){
                throw new WicketRuntimeException("Error setting field value of field "+this.field+" on object "+object+", value "+value,ex);
            }
        }
        public Class<?> getTargetClass(){
            return (Class<?>)this.field.getType();
        }
        public Field getField(){
            return this.field;
        }
    }
    private static class DefaultClassCache implements IClassCache{
        private final ConcurrentHashMap<Class<?>,Map<String,IGetAndSet>> map;
        private DefaultClassCache(){
            super();
            this.map=(ConcurrentHashMap<Class<?>,Map<String,IGetAndSet>>)Generics.newConcurrentHashMap(16);
        }
        public Map<String,IGetAndSet> get(final Class<?> clz){
            return this.map.get(clz);
        }
        public void put(final Class<?> clz,final Map<String,IGetAndSet> values){
            this.map.put(clz,values);
        }
    }
    public interface IGetAndSet{
        Object getValue(Object p0);
        Class<?> getTargetClass();
        Object newValue(Object p0);
        void setValue(Object p0,Object p1,PropertyResolverConverter p2);
        Field getField();
        Method getGetter();
        Method getSetter();
    }
    public interface IClassCache{
        void put(Class<?> p0,Map<String,IGetAndSet> p1);
        Map<String,IGetAndSet> get(Class<?> p0);
    }
}
