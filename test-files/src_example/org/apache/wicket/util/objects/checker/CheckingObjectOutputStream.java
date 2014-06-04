package org.apache.wicket.util.objects.checker;

import org.apache.wicket.util.lang.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.slf4j.*;
import org.apache.wicket.*;

public class CheckingObjectOutputStream extends ObjectOutputStream{
    private static final Logger log;
    private static final NoopOutputStream DUMMY_OUTPUT_STREAM;
    private static boolean available;
    private static Method LOOKUP_METHOD;
    private static Method GET_CLASS_DATA_LAYOUT_METHOD;
    private static Method GET_NUM_OBJ_FIELDS_METHOD;
    private static Method GET_OBJ_FIELD_VALUES_METHOD;
    private static Method GET_FIELD_METHOD;
    private static Method HAS_WRITE_REPLACE_METHOD_METHOD;
    private static Method INVOKE_WRITE_REPLACE_METHOD;
    private final IObjectChecker[] checkers;
    private final ObjectOutputStream out;
    private final LinkedList<TraceSlot> traceStack;
    private final Map<Object,Object> checked;
    private final LinkedList<CharSequence> nameStack;
    private Object root;
    private final Set<Class<?>> writeObjectMethodMissing;
    private CharSequence simpleName;
    private String fieldDescription;
    private final Stack<Object> stack;
    public static boolean isAvailable(){
        return CheckingObjectOutputStream.available;
    }
    public CheckingObjectOutputStream(final OutputStream outputStream,final IObjectChecker... checkers) throws IOException,SecurityException{
        super();
        this.traceStack=(LinkedList<TraceSlot>)new LinkedList();
        this.checked=(Map<Object,Object>)new IdentityHashMap();
        this.nameStack=(LinkedList<CharSequence>)new LinkedList();
        this.writeObjectMethodMissing=(Set<Class<?>>)new HashSet();
        this.simpleName=(CharSequence)"";
        this.stack=(Stack<Object>)new Stack();
        this.out=new ObjectOutputStream(outputStream);
        this.checkers=checkers;
    }
    private void check(final Object obj){
        if(obj==null){
            return;
        }
        try{
            if(this.stack.contains(obj)){
                return;
            }
        }
        catch(RuntimeException e){
            CheckingObjectOutputStream.log.warn("Wasn't possible to check the object '{}' possible due an problematic implementation of equals method",obj.getClass());
            return;
        }
        this.stack.push(obj);
        try{
            this.internalCheck(obj);
        }
        finally{
            this.stack.pop();
        }
    }
    private void internalCheck(Object obj){
        if(obj==null){
            return;
        }
        Class<?> cls=(Class<?>)obj.getClass();
        this.nameStack.add(this.simpleName);
        this.traceStack.add(new TraceSlot(obj,this.fieldDescription));
        for(final IObjectChecker checker : this.checkers){
            final IObjectChecker.Result result=checker.check(obj);
            if(result.status==IObjectChecker.Result.Status.FAILURE){
                final String prettyPrintMessage=this.toPrettyPrintedStack(Classes.name((Class)cls));
                final String exceptionMessage=result.reason+'\n'+prettyPrintMessage;
                throw new ObjectCheckException(exceptionMessage,result.cause);
            }
        }
        ObjectStreamClass desc=null;
        Label_0149:{
            break Label_0149;
            try{
                while(true){
                    desc=(ObjectStreamClass)CheckingObjectOutputStream.LOOKUP_METHOD.invoke(null,new Object[] { cls,Boolean.TRUE });
                    final Class<?> repCl;
                    if(!(boolean)CheckingObjectOutputStream.HAS_WRITE_REPLACE_METHOD_METHOD.invoke(desc,null)||(obj=CheckingObjectOutputStream.INVOKE_WRITE_REPLACE_METHOD.invoke(desc,new Object[] { obj }))==null||(repCl=(Class<?>)obj.getClass())==cls){
                        break;
                    }
                    cls=repCl;
                }
            }
            catch(IllegalAccessException e){
                throw new RuntimeException((Throwable)e);
            }
            catch(InvocationTargetException e2){
                throw new RuntimeException((Throwable)e2);
            }
        }
        if(!cls.isPrimitive()){
            class InterceptingObjectOutputStream extends ObjectOutputStream{
                private int counter;
                InterceptingObjectOutputStream() throws IOException{
                    super(CheckingObjectOutputStream.DUMMY_OUTPUT_STREAM);
                    this.enableReplaceObject(true);
                }
                protected Object replaceObject(final Object streamObj) throws IOException{
                    if(streamObj==original){
                        return streamObj;
                    }
                    ++this.counter;
                    if(CheckingObjectOutputStream.this.checked.containsKey(streamObj)){
                        return null;
                    }
                    CheckingObjectOutputStream.this.checked.put(streamObj,null);
                    final CharSequence arrayPos=new StringBuilder(10).append("[write:").append(this.counter).append(']');
                    CheckingObjectOutputStream.this.simpleName=arrayPos;
                    CheckingObjectOutputStream.access$484(CheckingObjectOutputStream.this,arrayPos);
                    CheckingObjectOutputStream.this.check(streamObj);
                    return streamObj;
                }
            }
            if(cls.isArray()){
                this.checked.put(obj,null);
                final Class<?> ccl=(Class<?>)cls.getComponentType();
                if(!ccl.isPrimitive()){
                    final Object[] objs=(Object[])obj;
                    for(int i=0;i<objs.length;++i){
                        final CharSequence arrayPos=new StringBuilder(4).append('[').append(i).append(']');
                        this.simpleName=arrayPos;
                        this.fieldDescription+=(Object)arrayPos;
                        this.check(objs[i]);
                    }
                }
            }
            else if(obj instanceof Externalizable&&!Proxy.isProxyClass(cls)){
                final Externalizable extObj=(Externalizable)obj;
                try{
                    extObj.writeExternal(new ObjectOutputAdaptor(){
                        private int count=0;
                        public void writeObject(final Object streamObj) throws IOException{
                            if(CheckingObjectOutputStream.this.checked.containsKey(streamObj)){
                                return;
                            }
                            CheckingObjectOutputStream.this.checked.put(streamObj,null);
                            final CharSequence arrayPos=new StringBuilder(10).append("[write:").append(this.count++).append(']');
                            CheckingObjectOutputStream.this.simpleName=arrayPos;
                            CheckingObjectOutputStream.access$484(CheckingObjectOutputStream.this,arrayPos);
                            CheckingObjectOutputStream.this.check(streamObj);
                        }
                    });
                }
                catch(Exception e3){
                    if(e3 instanceof ObjectCheckException){
                        throw (ObjectCheckException)e3;
                    }
                    CheckingObjectOutputStream.log.warn("Error delegating to Externalizable : {}, path: {}",e3.getMessage(),this.currentPath());
                }
            }
            else{
                Method writeObjectMethod=null;
                if(!this.writeObjectMethodMissing.contains(cls)){
                    try{
                        writeObjectMethod=cls.getDeclaredMethod("writeObject",new Class[] { ObjectOutputStream.class });
                    }
                    catch(SecurityException e7){
                        this.writeObjectMethodMissing.add(cls);
                    }
                    catch(NoSuchMethodException e8){
                        this.writeObjectMethodMissing.add(cls);
                    }
                }
                final Object original=obj;
                if(writeObjectMethod!=null){
                    try{
                        final InterceptingObjectOutputStream ioos=new InterceptingObjectOutputStream();
                        ioos.writeObject(obj);
                    }
                    catch(Exception e4){
                        if(e4 instanceof ObjectCheckException){
                            throw (ObjectCheckException)e4;
                        }
                        CheckingObjectOutputStream.log.warn("error delegating to writeObject : {}, path: {}",e4.getMessage(),this.currentPath());
                    }
                }
                else{
                    Object[] slots;
                    try{
                        slots=(Object[])CheckingObjectOutputStream.GET_CLASS_DATA_LAYOUT_METHOD.invoke(desc,null);
                    }
                    catch(Exception e5){
                        throw new RuntimeException((Throwable)e5);
                    }
                    for(final Object slot : slots){
                        ObjectStreamClass slotDesc;
                        try{
                            final Field descField=slot.getClass().getDeclaredField("desc");
                            descField.setAccessible(true);
                            slotDesc=(ObjectStreamClass)descField.get(slot);
                        }
                        catch(Exception e6){
                            throw new RuntimeException((Throwable)e6);
                        }
                        this.checked.put(obj,null);
                        this.checkFields(obj,slotDesc);
                    }
                }
            }
        }
        this.traceStack.removeLast();
        this.nameStack.removeLast();
    }
    private void checkFields(final Object obj,final ObjectStreamClass desc){
        int numFields;
        try{
            numFields=(int)CheckingObjectOutputStream.GET_NUM_OBJ_FIELDS_METHOD.invoke(desc,null);
        }
        catch(IllegalAccessException e){
            throw new RuntimeException((Throwable)e);
        }
        catch(InvocationTargetException e2){
            throw new RuntimeException((Throwable)e2);
        }
        if(numFields>0){
            final ObjectStreamField[] fields=desc.getFields();
            final Object[] objVals=new Object[numFields];
            final int numPrimFields=fields.length-objVals.length;
            try{
                CheckingObjectOutputStream.GET_OBJ_FIELD_VALUES_METHOD.invoke(desc,new Object[] { obj,objVals });
            }
            catch(IllegalAccessException e3){
                throw new RuntimeException((Throwable)e3);
            }
            catch(InvocationTargetException e4){
                throw new RuntimeException((Throwable)e4);
            }
            for(int i=0;i<objVals.length;++i){
                if(!(objVals[i] instanceof String)&&!(objVals[i] instanceof Number)&&!(objVals[i] instanceof Date)&&!(objVals[i] instanceof Boolean)){
                    if(!(objVals[i] instanceof Class)){
                        if(!this.checked.containsKey(objVals[i])){
                            final ObjectStreamField fieldDesc=fields[numPrimFields+i];
                            Field field;
                            try{
                                field=(Field)CheckingObjectOutputStream.GET_FIELD_METHOD.invoke(fieldDesc,null);
                            }
                            catch(IllegalAccessException e5){
                                throw new RuntimeException((Throwable)e5);
                            }
                            catch(InvocationTargetException e6){
                                throw new RuntimeException((Throwable)e6);
                            }
                            this.simpleName=(CharSequence)field.getName();
                            this.fieldDescription=field.toString();
                            this.check(objVals[i]);
                        }
                    }
                }
            }
        }
    }
    private StringBuilder currentPath(){
        final StringBuilder b=new StringBuilder();
        final Iterator<CharSequence> it=(Iterator<CharSequence>)this.nameStack.iterator();
        while(it.hasNext()){
            b.append((CharSequence)it.next());
            if(it.hasNext()){
                b.append('/');
            }
        }
        return b;
    }
    protected final String toPrettyPrintedStack(final String type){
        final StringBuilder result=new StringBuilder(512);
        final StringBuilder spaces=new StringBuilder(32);
        result.append("A problem occurred while checking object with type: ");
        result.append(type);
        result.append("\nField hierarchy is:");
        for(final TraceSlot slot : this.traceStack){
            spaces.append(' ').append(' ');
            result.append('\n').append((CharSequence)spaces).append(slot.fieldDescription);
            result.append(" [class=").append(Classes.name(slot.object.getClass()));
            if(slot.object instanceof Component){
                final Component component=(Component)slot.object;
                result.append(", path=").append(component.getPath());
            }
            result.append(']');
        }
        result.append(" <----- field that is causing the problem");
        return result.toString();
    }
    protected final void writeObjectOverride(final Object obj) throws IOException{
        if(!CheckingObjectOutputStream.available){
            return;
        }
        this.root=obj;
        if(this.fieldDescription==null){
            this.fieldDescription=((this.root instanceof Component)?((Component)this.root).getPath():"");
        }
        this.check(this.root);
        this.out.writeObject(obj);
    }
    public void reset() throws IOException{
        this.root=null;
        this.checked.clear();
        this.fieldDescription=null;
        this.simpleName=null;
        this.traceStack.clear();
        this.nameStack.clear();
        this.writeObjectMethodMissing.clear();
    }
    public void close() throws IOException{
        this.reset();
    }
    static /* synthetic */ String access$484(final CheckingObjectOutputStream x0,final Object x1){
        return x0.fieldDescription+=x1;
    }
    static{
        log=LoggerFactory.getLogger(CheckingObjectOutputStream.class);
        DUMMY_OUTPUT_STREAM=new NoopOutputStream();
        CheckingObjectOutputStream.available=true;
        try{
            (CheckingObjectOutputStream.LOOKUP_METHOD=ObjectStreamClass.class.getDeclaredMethod("lookup",new Class[] { Class.class,Boolean.TYPE })).setAccessible(true);
            (CheckingObjectOutputStream.GET_CLASS_DATA_LAYOUT_METHOD=ObjectStreamClass.class.getDeclaredMethod("getClassDataLayout",null)).setAccessible(true);
            (CheckingObjectOutputStream.GET_NUM_OBJ_FIELDS_METHOD=ObjectStreamClass.class.getDeclaredMethod("getNumObjFields",null)).setAccessible(true);
            (CheckingObjectOutputStream.GET_OBJ_FIELD_VALUES_METHOD=ObjectStreamClass.class.getDeclaredMethod("getObjFieldValues",new Class[] { Object.class,Object[].class })).setAccessible(true);
            (CheckingObjectOutputStream.GET_FIELD_METHOD=ObjectStreamField.class.getDeclaredMethod("getField",null)).setAccessible(true);
            (CheckingObjectOutputStream.HAS_WRITE_REPLACE_METHOD_METHOD=ObjectStreamClass.class.getDeclaredMethod("hasWriteReplaceMethod",null)).setAccessible(true);
            (CheckingObjectOutputStream.INVOKE_WRITE_REPLACE_METHOD=ObjectStreamClass.class.getDeclaredMethod("invokeWriteReplace",new Class[] { Object.class })).setAccessible(true);
        }
        catch(Exception e){
            CheckingObjectOutputStream.log.warn("SerializableChecker not available",e);
            CheckingObjectOutputStream.available=false;
        }
    }
    public static class ObjectCheckException extends WicketRuntimeException{
        public ObjectCheckException(final String message,final Throwable cause){
            super(message,cause);
        }
    }
    private static class NoopOutputStream extends OutputStream{
        public void close(){
        }
        public void flush(){
        }
        public void write(final byte[] b){
        }
        public void write(final byte[] b,final int i,final int l){
        }
        public void write(final int b){
        }
    }
    private abstract static class ObjectOutputAdaptor implements ObjectOutput{
        public void close() throws IOException{
        }
        public void flush() throws IOException{
        }
        public void write(final byte[] b) throws IOException{
        }
        public void write(final byte[] b,final int off,final int len) throws IOException{
        }
        public void write(final int b) throws IOException{
        }
        public void writeBoolean(final boolean v) throws IOException{
        }
        public void writeByte(final int v) throws IOException{
        }
        public void writeBytes(final String s) throws IOException{
        }
        public void writeChar(final int v) throws IOException{
        }
        public void writeChars(final String s) throws IOException{
        }
        public void writeDouble(final double v) throws IOException{
        }
        public void writeFloat(final float v) throws IOException{
        }
        public void writeInt(final int v) throws IOException{
        }
        public void writeLong(final long v) throws IOException{
        }
        public void writeShort(final int v) throws IOException{
        }
        public void writeUTF(final String str) throws IOException{
        }
    }
    private static final class TraceSlot{
        private final String fieldDescription;
        private final Object object;
        TraceSlot(final Object object,final String fieldDescription){
            super();
            this.object=object;
            this.fieldDescription=fieldDescription;
        }
        public String toString(){
            return this.object.getClass()+" - "+this.fieldDescription;
        }
    }
}
