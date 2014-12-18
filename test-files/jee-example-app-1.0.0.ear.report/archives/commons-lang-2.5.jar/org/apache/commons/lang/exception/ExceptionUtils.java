package org.apache.commons.lang.exception;

import org.apache.commons.lang.ClassUtils;
import java.util.StringTokenizer;
import org.apache.commons.lang.SystemUtils;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.sql.SQLException;
import org.apache.commons.lang.exception.Nestable;
import org.apache.commons.lang.ArrayUtils;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import org.apache.commons.lang.NullArgumentException;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class ExceptionUtils{
    static final String WRAPPED_MARKER=" [wrapped] ";
    private static final Object CAUSE_METHOD_NAMES_LOCK;
    private static String[] CAUSE_METHOD_NAMES;
    private static final Method THROWABLE_CAUSE_METHOD;
    private static final Method THROWABLE_INITCAUSE_METHOD;
    static /* synthetic */ Class class$java$lang$Throwable;
    public static void addCauseMethodName(final String methodName){
        if(StringUtils.isNotEmpty(methodName)&&!isCauseMethodName(methodName)){
            final List list=getCauseMethodNameList();
            if(list.add(methodName)){
                synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
                    ExceptionUtils.CAUSE_METHOD_NAMES=toArray(list);
                }
            }
        }
    }
    public static void removeCauseMethodName(final String methodName){
        if(StringUtils.isNotEmpty(methodName)){
            final List list=getCauseMethodNameList();
            if(list.remove(methodName)){
                synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
                    ExceptionUtils.CAUSE_METHOD_NAMES=toArray(list);
                }
            }
        }
    }
    public static boolean setCause(final Throwable target,final Throwable cause){
        if(target==null){
            throw new NullArgumentException("target");
        }
        final Object[] causeArgs= { cause };
        boolean modifiedTarget=false;
        if(ExceptionUtils.THROWABLE_INITCAUSE_METHOD!=null){
            try{
                ExceptionUtils.THROWABLE_INITCAUSE_METHOD.invoke(target,causeArgs);
                modifiedTarget=true;
            }
            catch(IllegalAccessException ignored){
            }
            catch(InvocationTargetException ex){
            }
        }
        try{
            final Method setCauseMethod=target.getClass().getMethod("setCause",(ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable);
            setCauseMethod.invoke(target,causeArgs);
            modifiedTarget=true;
        }
        catch(NoSuchMethodException ignored2){
        }
        catch(IllegalAccessException ignored){
        }
        catch(InvocationTargetException ex2){
        }
        return modifiedTarget;
    }
    private static String[] toArray(final List list){
        return list.toArray(new String[list.size()]);
    }
    private static ArrayList getCauseMethodNameList(){
        synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
            return new ArrayList((Collection<? extends E>)Arrays.asList(ExceptionUtils.CAUSE_METHOD_NAMES));
        }
    }
    public static boolean isCauseMethodName(final String methodName){
        synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
            return ArrayUtils.indexOf(ExceptionUtils.CAUSE_METHOD_NAMES,methodName)>=0;
        }
    }
    public static Throwable getCause(final Throwable throwable){
        synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
            return getCause(throwable,ExceptionUtils.CAUSE_METHOD_NAMES);
        }
    }
    public static Throwable getCause(final Throwable throwable,String[] methodNames){
        if(throwable==null){
            return null;
        }
        Throwable cause=getCauseUsingWellKnownTypes(throwable);
        if(cause==null){
            if(methodNames==null){
                synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
                    methodNames=ExceptionUtils.CAUSE_METHOD_NAMES;
                }
            }
            for(int i=0;i<methodNames.length;++i){
                final String methodName=methodNames[i];
                if(methodName!=null){
                    cause=getCauseUsingMethodName(throwable,methodName);
                    if(cause!=null){
                        break;
                    }
                }
            }
            if(cause==null){
                cause=getCauseUsingFieldName(throwable,"detail");
            }
        }
        return cause;
    }
    public static Throwable getRootCause(final Throwable throwable){
        final List list=getThrowableList(throwable);
        return (list.size()<2)?null:list.get(list.size()-1);
    }
    private static Throwable getCauseUsingWellKnownTypes(final Throwable throwable){
        if(throwable instanceof Nestable){
            return ((Nestable)throwable).getCause();
        }
        if(throwable instanceof SQLException){
            return ((SQLException)throwable).getNextException();
        }
        if(throwable instanceof InvocationTargetException){
            return ((InvocationTargetException)throwable).getTargetException();
        }
        return null;
    }
    private static Throwable getCauseUsingMethodName(final Throwable throwable,final String methodName){
        Method method=null;
        try{
            method=throwable.getClass().getMethod(methodName,(Class<?>[])null);
        }
        catch(NoSuchMethodException ignored){
        }
        catch(SecurityException ex){
        }
        if(method!=null&&((ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable).isAssignableFrom(method.getReturnType())){
            try{
                return (Throwable)method.invoke(throwable,ArrayUtils.EMPTY_OBJECT_ARRAY);
            }
            catch(IllegalAccessException ignored2){
            }
            catch(IllegalArgumentException ignored3){
            }
            catch(InvocationTargetException ex2){
            }
        }
        return null;
    }
    private static Throwable getCauseUsingFieldName(final Throwable throwable,final String fieldName){
        Field field=null;
        try{
            field=throwable.getClass().getField(fieldName);
        }
        catch(NoSuchFieldException ignored){
        }
        catch(SecurityException ex){
        }
        if(field!=null&&((ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable).isAssignableFrom(field.getType())){
            try{
                return (Throwable)field.get(throwable);
            }
            catch(IllegalAccessException ignored2){
            }
            catch(IllegalArgumentException ex2){
            }
        }
        return null;
    }
    public static boolean isThrowableNested(){
        return ExceptionUtils.THROWABLE_CAUSE_METHOD!=null;
    }
    public static boolean isNestedThrowable(final Throwable throwable){
        if(throwable==null){
            return false;
        }
        if(throwable instanceof Nestable){
            return true;
        }
        if(throwable instanceof SQLException){
            return true;
        }
        if(throwable instanceof InvocationTargetException){
            return true;
        }
        if(isThrowableNested()){
            return true;
        }
        final Class cls=throwable.getClass();
        synchronized(ExceptionUtils.CAUSE_METHOD_NAMES_LOCK){
            for(int i=0,isize=ExceptionUtils.CAUSE_METHOD_NAMES.length;i<isize;++i){
                try{
                    final Method method=cls.getMethod(ExceptionUtils.CAUSE_METHOD_NAMES[i],(Class[])null);
                    if(method!=null&&((ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable).isAssignableFrom(method.getReturnType())){
                        return true;
                    }
                }
                catch(NoSuchMethodException ignored){
                }
                catch(SecurityException ex){
                }
            }
        }
        try{
            final Field field=cls.getField("detail");
            if(field!=null){
                return true;
            }
        }
        catch(NoSuchFieldException ignored2){
        }
        catch(SecurityException ex2){
        }
        return false;
    }
    public static int getThrowableCount(final Throwable throwable){
        return getThrowableList(throwable).size();
    }
    public static Throwable[] getThrowables(final Throwable throwable){
        final List list=getThrowableList(throwable);
        return list.toArray(new Throwable[list.size()]);
    }
    public static List getThrowableList(Throwable throwable){
        List list;
        for(list=new ArrayList();throwable!=null&&!list.contains(throwable);throwable=getCause(throwable)){
            list.add(throwable);
        }
        return list;
    }
    public static int indexOfThrowable(final Throwable throwable,final Class clazz){
        return indexOf(throwable,clazz,0,false);
    }
    public static int indexOfThrowable(final Throwable throwable,final Class clazz,final int fromIndex){
        return indexOf(throwable,clazz,fromIndex,false);
    }
    public static int indexOfType(final Throwable throwable,final Class type){
        return indexOf(throwable,type,0,true);
    }
    public static int indexOfType(final Throwable throwable,final Class type,final int fromIndex){
        return indexOf(throwable,type,fromIndex,true);
    }
    private static int indexOf(final Throwable throwable,final Class type,int fromIndex,final boolean subclass){
        if(throwable==null||type==null){
            return -1;
        }
        if(fromIndex<0){
            fromIndex=0;
        }
        final Throwable[] throwables=getThrowables(throwable);
        if(fromIndex>=throwables.length){
            return -1;
        }
        if(subclass){
            for(int i=fromIndex;i<throwables.length;++i){
                if(type.isAssignableFrom(throwables[i].getClass())){
                    return i;
                }
            }
        }
        else{
            for(int i=fromIndex;i<throwables.length;++i){
                if(type.equals(throwables[i].getClass())){
                    return i;
                }
            }
        }
        return -1;
    }
    public static void printRootCauseStackTrace(final Throwable throwable){
        printRootCauseStackTrace(throwable,System.err);
    }
    public static void printRootCauseStackTrace(final Throwable throwable,final PrintStream stream){
        if(throwable==null){
            return;
        }
        if(stream==null){
            throw new IllegalArgumentException("The PrintStream must not be null");
        }
        final String[] trace=getRootCauseStackTrace(throwable);
        for(int i=0;i<trace.length;++i){
            stream.println(trace[i]);
        }
        stream.flush();
    }
    public static void printRootCauseStackTrace(final Throwable throwable,final PrintWriter writer){
        if(throwable==null){
            return;
        }
        if(writer==null){
            throw new IllegalArgumentException("The PrintWriter must not be null");
        }
        final String[] trace=getRootCauseStackTrace(throwable);
        for(int i=0;i<trace.length;++i){
            writer.println(trace[i]);
        }
        writer.flush();
    }
    public static String[] getRootCauseStackTrace(final Throwable throwable){
        if(throwable==null){
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        final Throwable[] throwables=getThrowables(throwable);
        final int count=throwables.length;
        final ArrayList frames=new ArrayList();
        List nextTrace=getStackFrameList(throwables[count-1]);
        int i=count;
        while(--i>=0){
            final List trace=nextTrace;
            if(i!=0){
                nextTrace=getStackFrameList(throwables[i-1]);
                removeCommonFrames(trace,nextTrace);
            }
            if(i==count-1){
                frames.add(throwables[i].toString());
            }
            else{
                frames.add(" [wrapped] "+throwables[i].toString());
            }
            for(int j=0;j<trace.size();++j){
                frames.add(trace.get(j));
            }
        }
        return frames.toArray(new String[0]);
    }
    public static void removeCommonFrames(final List causeFrames,final List wrapperFrames){
        if(causeFrames==null||wrapperFrames==null){
            throw new IllegalArgumentException("The List must not be null");
        }
        for(int causeFrameIndex=causeFrames.size()-1,wrapperFrameIndex=wrapperFrames.size()-1;causeFrameIndex>=0&&wrapperFrameIndex>=0;--causeFrameIndex,--wrapperFrameIndex){
            final String causeFrame=causeFrames.get(causeFrameIndex);
            final String wrapperFrame=wrapperFrames.get(wrapperFrameIndex);
            if(causeFrame.equals(wrapperFrame)){
                causeFrames.remove(causeFrameIndex);
            }
        }
    }
    public static String getFullStackTrace(final Throwable throwable){
        final StringWriter sw=new StringWriter();
        final PrintWriter pw=new PrintWriter(sw,true);
        final Throwable[] ts=getThrowables(throwable);
        for(int i=0;i<ts.length;++i){
            ts[i].printStackTrace(pw);
            if(isNestedThrowable(ts[i])){
                break;
            }
        }
        return sw.getBuffer().toString();
    }
    public static String getStackTrace(final Throwable throwable){
        final StringWriter sw=new StringWriter();
        final PrintWriter pw=new PrintWriter(sw,true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
    public static String[] getStackFrames(final Throwable throwable){
        if(throwable==null){
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return getStackFrames(getStackTrace(throwable));
    }
    static String[] getStackFrames(final String stackTrace){
        final String linebreak=SystemUtils.LINE_SEPARATOR;
        final StringTokenizer frames=new StringTokenizer(stackTrace,linebreak);
        final List list=new ArrayList();
        while(frames.hasMoreTokens()){
            list.add(frames.nextToken());
        }
        return toArray(list);
    }
    static List getStackFrameList(final Throwable t){
        final String stackTrace=getStackTrace(t);
        final String linebreak=SystemUtils.LINE_SEPARATOR;
        final StringTokenizer frames=new StringTokenizer(stackTrace,linebreak);
        final List list=new ArrayList();
        boolean traceStarted=false;
        while(frames.hasMoreTokens()){
            final String token=frames.nextToken();
            final int at=token.indexOf("at");
            if(at!=-1&&token.substring(0,at).trim().length()==0){
                traceStarted=true;
                list.add(token);
            }
            else{
                if(traceStarted){
                    break;
                }
                continue;
            }
        }
        return list;
    }
    public static String getMessage(final Throwable th){
        if(th==null){
            return "";
        }
        final String clsName=ClassUtils.getShortClassName(th,null);
        final String msg=th.getMessage();
        return clsName+": "+StringUtils.defaultString(msg);
    }
    public static String getRootCauseMessage(final Throwable th){
        Throwable root=getRootCause(th);
        root=((root==null)?th:root);
        return getMessage(root);
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
        CAUSE_METHOD_NAMES_LOCK=new Object();
        ExceptionUtils.CAUSE_METHOD_NAMES=new String[] { "getCause","getNextException","getTargetException","getException","getSourceException","getRootCause","getCausedByException","getNested","getLinkedException","getNestedException","getLinkedCause","getThrowable" };
        Method causeMethod;
        try{
            causeMethod=((ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable).getMethod("getCause",(Class[])null);
        }
        catch(Exception e){
            causeMethod=null;
        }
        THROWABLE_CAUSE_METHOD=causeMethod;
        try{
            causeMethod=((ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable).getMethod("initCause",(ExceptionUtils.class$java$lang$Throwable==null)?(ExceptionUtils.class$java$lang$Throwable=class$("java.lang.Throwable")):ExceptionUtils.class$java$lang$Throwable);
        }
        catch(Exception e){
            causeMethod=null;
        }
        THROWABLE_INITCAUSE_METHOD=causeMethod;
    }
}
