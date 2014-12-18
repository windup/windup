package org.apache.commons.lang;

import java.util.HashMap;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import java.util.Collection;
import org.apache.commons.lang.ArrayUtils;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassUtils{
    public static final char PACKAGE_SEPARATOR_CHAR='.';
    public static final String PACKAGE_SEPARATOR;
    public static final char INNER_CLASS_SEPARATOR_CHAR='$';
    public static final String INNER_CLASS_SEPARATOR;
    private static final Map primitiveWrapperMap;
    private static final Map wrapperPrimitiveMap;
    private static final Map abbreviationMap;
    private static final Map reverseAbbreviationMap;
    static /* synthetic */ Class class$java$lang$Boolean;
    static /* synthetic */ Class class$java$lang$Byte;
    static /* synthetic */ Class class$java$lang$Character;
    static /* synthetic */ Class class$java$lang$Short;
    static /* synthetic */ Class class$java$lang$Integer;
    static /* synthetic */ Class class$java$lang$Long;
    static /* synthetic */ Class class$java$lang$Double;
    static /* synthetic */ Class class$java$lang$Float;
    static /* synthetic */ Class class$org$apache$commons$lang$ClassUtils;
    private static void addAbbreviation(final String primitive,final String abbreviation){
        ClassUtils.abbreviationMap.put(primitive,abbreviation);
        ClassUtils.reverseAbbreviationMap.put(abbreviation,primitive);
    }
    public static String getShortClassName(final Object object,final String valueIfNull){
        if(object==null){
            return valueIfNull;
        }
        return getShortClassName(object.getClass());
    }
    public static String getShortClassName(final Class cls){
        if(cls==null){
            return "";
        }
        return getShortClassName(cls.getName());
    }
    public static String getShortClassName(String className){
        if(className==null){
            return "";
        }
        if(className.length()==0){
            return "";
        }
        final StringBuffer arrayPrefix=new StringBuffer();
        if(className.startsWith("[")){
            while(className.charAt(0)=='['){
                className=className.substring(1);
                arrayPrefix.append("[]");
            }
            if(className.charAt(0)=='L'&&className.charAt(className.length()-1)==';'){
                className=className.substring(1,className.length()-1);
            }
        }
        if(ClassUtils.reverseAbbreviationMap.containsKey(className)){
            className=ClassUtils.reverseAbbreviationMap.get(className);
        }
        final int lastDotIdx=className.lastIndexOf(46);
        final int innerIdx=className.indexOf(36,(lastDotIdx==-1)?0:(lastDotIdx+1));
        String out=className.substring(lastDotIdx+1);
        if(innerIdx!=-1){
            out=out.replace('$','.');
        }
        return out+(Object)arrayPrefix;
    }
    public static String getPackageName(final Object object,final String valueIfNull){
        if(object==null){
            return valueIfNull;
        }
        return getPackageName(object.getClass());
    }
    public static String getPackageName(final Class cls){
        if(cls==null){
            return "";
        }
        return getPackageName(cls.getName());
    }
    public static String getPackageName(String className){
        if(className==null||className.length()==0){
            return "";
        }
        while(className.charAt(0)=='['){
            className=className.substring(1);
        }
        if(className.charAt(0)=='L'&&className.charAt(className.length()-1)==';'){
            className=className.substring(1);
        }
        final int i=className.lastIndexOf(46);
        if(i==-1){
            return "";
        }
        return className.substring(0,i);
    }
    public static List getAllSuperclasses(final Class cls){
        if(cls==null){
            return null;
        }
        final List classes=new ArrayList();
        for(Class superclass=cls.getSuperclass();superclass!=null;superclass=superclass.getSuperclass()){
            classes.add(superclass);
        }
        return classes;
    }
    public static List getAllInterfaces(final Class cls){
        if(cls==null){
            return null;
        }
        final List interfacesFound=new ArrayList();
        getAllInterfaces(cls,interfacesFound);
        return interfacesFound;
    }
    private static void getAllInterfaces(Class cls,final List interfacesFound){
        while(cls!=null){
            final Class[] interfaces=cls.getInterfaces();
            for(int i=0;i<interfaces.length;++i){
                if(!interfacesFound.contains(interfaces[i])){
                    interfacesFound.add(interfaces[i]);
                    getAllInterfaces(interfaces[i],interfacesFound);
                }
            }
            cls=cls.getSuperclass();
        }
    }
    public static List convertClassNamesToClasses(final List classNames){
        if(classNames==null){
            return null;
        }
        final List classes=new ArrayList(classNames.size());
        for(final String className : classNames){
            try{
                classes.add(Class.forName(className));
            }
            catch(Exception ex){
                classes.add(null);
            }
        }
        return classes;
    }
    public static List convertClassesToClassNames(final List classes){
        if(classes==null){
            return null;
        }
        final List classNames=new ArrayList(classes.size());
        for(final Class cls : classes){
            if(cls==null){
                classNames.add(null);
            }
            else{
                classNames.add(cls.getName());
            }
        }
        return classNames;
    }
    public static boolean isAssignable(final Class[] classArray,final Class[] toClassArray){
        return isAssignable(classArray,toClassArray,false);
    }
    public static boolean isAssignable(Class[] classArray,Class[] toClassArray,final boolean autoboxing){
        if(!ArrayUtils.isSameLength(classArray,toClassArray)){
            return false;
        }
        if(classArray==null){
            classArray=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        if(toClassArray==null){
            toClassArray=ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        for(int i=0;i<classArray.length;++i){
            if(!isAssignable(classArray[i],toClassArray[i],autoboxing)){
                return false;
            }
        }
        return true;
    }
    public static boolean isAssignable(final Class cls,final Class toClass){
        return isAssignable(cls,toClass,false);
    }
    public static boolean isAssignable(Class cls,final Class toClass,final boolean autoboxing){
        if(toClass==null){
            return false;
        }
        if(cls==null){
            return !toClass.isPrimitive();
        }
        if(autoboxing){
            if(cls.isPrimitive()&&!toClass.isPrimitive()){
                cls=primitiveToWrapper(cls);
                if(cls==null){
                    return false;
                }
            }
            if(toClass.isPrimitive()&&!cls.isPrimitive()){
                cls=wrapperToPrimitive(cls);
                if(cls==null){
                    return false;
                }
            }
        }
        if(cls.equals(toClass)){
            return true;
        }
        if(!cls.isPrimitive()){
            return toClass.isAssignableFrom(cls);
        }
        if(!toClass.isPrimitive()){
            return false;
        }
        if(Integer.TYPE.equals(cls)){
            return Long.TYPE.equals(toClass)||Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass);
        }
        if(Long.TYPE.equals(cls)){
            return Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass);
        }
        if(Boolean.TYPE.equals(cls)){
            return false;
        }
        if(Double.TYPE.equals(cls)){
            return false;
        }
        if(Float.TYPE.equals(cls)){
            return Double.TYPE.equals(toClass);
        }
        if(Character.TYPE.equals(cls)){
            return Integer.TYPE.equals(toClass)||Long.TYPE.equals(toClass)||Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass);
        }
        if(Short.TYPE.equals(cls)){
            return Integer.TYPE.equals(toClass)||Long.TYPE.equals(toClass)||Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass);
        }
        return Byte.TYPE.equals(cls)&&(Short.TYPE.equals(toClass)||Integer.TYPE.equals(toClass)||Long.TYPE.equals(toClass)||Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass));
    }
    public static Class primitiveToWrapper(final Class cls){
        Class convertedClass=cls;
        if(cls!=null&&cls.isPrimitive()){
            convertedClass=(Class)ClassUtils.primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }
    public static Class[] primitivesToWrappers(final Class[] classes){
        if(classes==null){
            return null;
        }
        if(classes.length==0){
            return classes;
        }
        final Class[] convertedClasses=new Class[classes.length];
        for(int i=0;i<classes.length;++i){
            convertedClasses[i]=primitiveToWrapper(classes[i]);
        }
        return convertedClasses;
    }
    public static Class wrapperToPrimitive(final Class cls){
        return ClassUtils.wrapperPrimitiveMap.get(cls);
    }
    public static Class[] wrappersToPrimitives(final Class[] classes){
        if(classes==null){
            return null;
        }
        if(classes.length==0){
            return classes;
        }
        final Class[] convertedClasses=new Class[classes.length];
        for(int i=0;i<classes.length;++i){
            convertedClasses[i]=wrapperToPrimitive(classes[i]);
        }
        return convertedClasses;
    }
    public static boolean isInnerClass(final Class cls){
        return cls!=null&&cls.getName().indexOf(36)>=0;
    }
    public static Class getClass(final ClassLoader classLoader,final String className,final boolean initialize) throws ClassNotFoundException{
        Class clazz;
        if(ClassUtils.abbreviationMap.containsKey(className)){
            final String clsName="["+ClassUtils.abbreviationMap.get(className);
            clazz=Class.forName(clsName,initialize,classLoader).getComponentType();
        }
        else{
            clazz=Class.forName(toCanonicalName(className),initialize,classLoader);
        }
        return clazz;
    }
    public static Class getClass(final ClassLoader classLoader,final String className) throws ClassNotFoundException{
        return getClass(classLoader,className,true);
    }
    public static Class getClass(final String className) throws ClassNotFoundException{
        return getClass(className,true);
    }
    public static Class getClass(final String className,final boolean initialize) throws ClassNotFoundException{
        final ClassLoader contextCL=Thread.currentThread().getContextClassLoader();
        final ClassLoader loader=(contextCL==null)?((ClassUtils.class$org$apache$commons$lang$ClassUtils==null)?(ClassUtils.class$org$apache$commons$lang$ClassUtils=class$("org.apache.commons.lang.ClassUtils")):ClassUtils.class$org$apache$commons$lang$ClassUtils).getClassLoader():contextCL;
        return getClass(loader,className,initialize);
    }
    public static Method getPublicMethod(final Class cls,final String methodName,final Class[] parameterTypes) throws SecurityException,NoSuchMethodException{
        final Method declaredMethod=cls.getMethod(methodName,(Class[])parameterTypes);
        if(Modifier.isPublic(declaredMethod.getDeclaringClass().getModifiers())){
            return declaredMethod;
        }
        final List candidateClasses=new ArrayList();
        candidateClasses.addAll(getAllInterfaces(cls));
        candidateClasses.addAll(getAllSuperclasses(cls));
        for(final Class candidateClass : candidateClasses){
            if(!Modifier.isPublic(candidateClass.getModifiers())){
                continue;
            }
            Method candidateMethod;
            try{
                candidateMethod=candidateClass.getMethod(methodName,(Class[])parameterTypes);
            }
            catch(NoSuchMethodException ex){
                continue;
            }
            if(Modifier.isPublic(candidateMethod.getDeclaringClass().getModifiers())){
                return candidateMethod;
            }
        }
        throw new NoSuchMethodException("Can't find a public method for "+methodName+" "+ArrayUtils.toString(parameterTypes));
    }
    private static String toCanonicalName(String className){
        className=StringUtils.deleteWhitespace(className);
        if(className==null){
            throw new NullArgumentException("className");
        }
        if(className.endsWith("[]")){
            final StringBuffer classNameBuffer=new StringBuffer();
            while(className.endsWith("[]")){
                className=className.substring(0,className.length()-2);
                classNameBuffer.append("[");
            }
            final String abbreviation=ClassUtils.abbreviationMap.get(className);
            if(abbreviation!=null){
                classNameBuffer.append(abbreviation);
            }
            else{
                classNameBuffer.append("L").append(className).append(";");
            }
            className=classNameBuffer.toString();
        }
        return className;
    }
    public static Class[] toClass(final Object[] array){
        if(array==null){
            return null;
        }
        if(array.length==0){
            return ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        final Class[] classes=new Class[array.length];
        for(int i=0;i<array.length;++i){
            classes[i]=((array[i]==null)?null:array[i].getClass());
        }
        return classes;
    }
    public static String getShortCanonicalName(final Object object,final String valueIfNull){
        if(object==null){
            return valueIfNull;
        }
        return getShortCanonicalName(object.getClass().getName());
    }
    public static String getShortCanonicalName(final Class cls){
        if(cls==null){
            return "";
        }
        return getShortCanonicalName(cls.getName());
    }
    public static String getShortCanonicalName(final String canonicalName){
        return getShortClassName(getCanonicalName(canonicalName));
    }
    public static String getPackageCanonicalName(final Object object,final String valueIfNull){
        if(object==null){
            return valueIfNull;
        }
        return getPackageCanonicalName(object.getClass().getName());
    }
    public static String getPackageCanonicalName(final Class cls){
        if(cls==null){
            return "";
        }
        return getPackageCanonicalName(cls.getName());
    }
    public static String getPackageCanonicalName(final String canonicalName){
        return getPackageName(getCanonicalName(canonicalName));
    }
    private static String getCanonicalName(String className){
        className=StringUtils.deleteWhitespace(className);
        if(className==null){
            return null;
        }
        int dim=0;
        while(className.startsWith("[")){
            ++dim;
            className=className.substring(1);
        }
        if(dim<1){
            return className;
        }
        if(className.startsWith("L")){
            className=className.substring(1,className.endsWith(";")?(className.length()-1):className.length());
        }
        else if(className.length()>0){
            className=ClassUtils.reverseAbbreviationMap.get(className.substring(0,1));
        }
        final StringBuffer canonicalClassNameBuffer=new StringBuffer(className);
        for(int i=0;i<dim;++i){
            canonicalClassNameBuffer.append("[]");
        }
        return canonicalClassNameBuffer.toString();
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        PACKAGE_SEPARATOR=String.valueOf('.');
        INNER_CLASS_SEPARATOR=String.valueOf('$');
        (primitiveWrapperMap=new HashMap()).put(Boolean.TYPE,(ClassUtils.class$java$lang$Boolean==null)?(ClassUtils.class$java$lang$Boolean=class$("java.lang.Boolean")):ClassUtils.class$java$lang$Boolean);
        ClassUtils.primitiveWrapperMap.put(Byte.TYPE,(ClassUtils.class$java$lang$Byte==null)?(ClassUtils.class$java$lang$Byte=class$("java.lang.Byte")):ClassUtils.class$java$lang$Byte);
        ClassUtils.primitiveWrapperMap.put(Character.TYPE,(ClassUtils.class$java$lang$Character==null)?(ClassUtils.class$java$lang$Character=class$("java.lang.Character")):ClassUtils.class$java$lang$Character);
        ClassUtils.primitiveWrapperMap.put(Short.TYPE,(ClassUtils.class$java$lang$Short==null)?(ClassUtils.class$java$lang$Short=class$("java.lang.Short")):ClassUtils.class$java$lang$Short);
        ClassUtils.primitiveWrapperMap.put(Integer.TYPE,(ClassUtils.class$java$lang$Integer==null)?(ClassUtils.class$java$lang$Integer=class$("java.lang.Integer")):ClassUtils.class$java$lang$Integer);
        ClassUtils.primitiveWrapperMap.put(Long.TYPE,(ClassUtils.class$java$lang$Long==null)?(ClassUtils.class$java$lang$Long=class$("java.lang.Long")):ClassUtils.class$java$lang$Long);
        ClassUtils.primitiveWrapperMap.put(Double.TYPE,(ClassUtils.class$java$lang$Double==null)?(ClassUtils.class$java$lang$Double=class$("java.lang.Double")):ClassUtils.class$java$lang$Double);
        ClassUtils.primitiveWrapperMap.put(Float.TYPE,(ClassUtils.class$java$lang$Float==null)?(ClassUtils.class$java$lang$Float=class$("java.lang.Float")):ClassUtils.class$java$lang$Float);
        ClassUtils.primitiveWrapperMap.put(Void.TYPE,Void.TYPE);
        wrapperPrimitiveMap=new HashMap();
        for(final Class primitiveClass : ClassUtils.primitiveWrapperMap.keySet()){
            final Class wrapperClass=ClassUtils.primitiveWrapperMap.get(primitiveClass);
            if(!primitiveClass.equals(wrapperClass)){
                ClassUtils.wrapperPrimitiveMap.put(wrapperClass,primitiveClass);
            }
        }
        abbreviationMap=new HashMap();
        reverseAbbreviationMap=new HashMap();
        addAbbreviation("int","I");
        addAbbreviation("boolean","Z");
        addAbbreviation("float","F");
        addAbbreviation("long","J");
        addAbbreviation("short","S");
        addAbbreviation("byte","B");
        addAbbreviation("double","D");
        addAbbreviation("char","C");
    }
}
