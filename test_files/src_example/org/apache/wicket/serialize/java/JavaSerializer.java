package org.apache.wicket.serialize.java;

import org.apache.wicket.serialize.*;
import org.slf4j.*;
import org.apache.wicket.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.application.*;
import org.apache.wicket.util.objects.checker.*;
import org.apache.wicket.util.io.*;
import java.io.*;

public class JavaSerializer implements ISerializer{
    private static final Logger log;
    private final String applicationKey;
    public JavaSerializer(final String applicationKey){
        super();
        this.applicationKey=applicationKey;
    }
    public byte[] serialize(final Object object){
        try{
            final ByteArrayOutputStream out=new ByteArrayOutputStream();
            ObjectOutputStream oos=null;
            try{
                oos=this.newObjectOutputStream(out);
                oos.writeObject(this.applicationKey);
                oos.writeObject(object);
            }
            finally{
                try{
                    IOUtils.close((Closeable)oos);
                }
                finally{
                    out.close();
                }
            }
            return out.toByteArray();
        }
        catch(Exception e){
            JavaSerializer.log.error("Error serializing object "+object.getClass()+" [object="+object+"]",e);
            return null;
        }
    }
    public Object deserialize(final byte[] data){
        final ThreadContext old=ThreadContext.get(false);
        final ByteArrayInputStream in=new ByteArrayInputStream(data);
        ObjectInputStream ois=null;
        try{
            final Application oldApplication=ThreadContext.getApplication();
            try{
                ois=this.newObjectInputStream(in);
                final String applicationName=(String)ois.readObject();
                if(applicationName!=null){
                    final Application app=Application.get(applicationName);
                    if(app!=null){
                        ThreadContext.setApplication(app);
                    }
                }
                return ois.readObject();
            }
            finally{
                try{
                    ThreadContext.setApplication(oldApplication);
                    IOUtils.close((Closeable)ois);
                }
                finally{
                    in.close();
                }
            }
        }
        catch(ClassNotFoundException e){
            throw new RuntimeException("Could not deserialize object using: "+ois.getClass(),(Throwable)e);
        }
        catch(IOException e2){
            throw new RuntimeException("Could not deserialize object using: "+ois.getClass(),(Throwable)e2);
        }
        finally{
            ThreadContext.restore(old);
        }
    }
    protected ObjectInputStream newObjectInputStream(final InputStream in) throws IOException{
        return new ClassResolverObjectInputStream(in);
    }
    protected ObjectOutputStream newObjectOutputStream(final OutputStream out) throws IOException{
        return new SerializationCheckingObjectOutputStream(out);
    }
    static{
        log=LoggerFactory.getLogger(JavaSerializer.class);
    }
    private static class ClassResolverObjectInputStream extends ObjectInputStream{
        public ClassResolverObjectInputStream(final InputStream in) throws IOException{
            super(in);
        }
        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException,ClassNotFoundException{
            final String className=desc.getName();
            try{
                return (Class<?>)super.resolveClass(desc);
            }
            catch(ClassNotFoundException ex2){
                JavaSerializer.log.debug("Class not found by the object outputstream itself, trying the IClassResolver");
                Class<?> candidate=null;
                try{
                    final Application application=Application.get();
                    final IApplicationSettings applicationSettings=application.getApplicationSettings();
                    final IClassResolver classResolver=applicationSettings.getClassResolver();
                    candidate=classResolver.resolveClass(className);
                    if(candidate==null){
                        candidate=(Class<?>)super.resolveClass(desc);
                    }
                }
                catch(WicketRuntimeException ex){
                    if(ex.getCause() instanceof ClassNotFoundException){
                        throw (ClassNotFoundException)ex.getCause();
                    }
                }
                return candidate;
            }
        }
    }
    private static class SerializationCheckingObjectOutputStream extends ObjectOutputStream{
        private final OutputStream outputStream;
        private final ObjectOutputStream oos;
        private SerializationCheckingObjectOutputStream(final OutputStream outputStream) throws IOException{
            super();
            this.outputStream=outputStream;
            this.oos=new ObjectOutputStream(outputStream);
        }
        protected final void writeObjectOverride(final Object obj) throws IOException{
            try{
                this.oos.writeObject(obj);
            }
            catch(NotSerializableException nsx){
                if(CheckingObjectOutputStream.isAvailable()){
                    new SerializableChecker(this.outputStream,nsx).writeObject(obj);
                    throw nsx;
                }
                throw nsx;
            }
            catch(Exception e){
                JavaSerializer.log.error("error writing object "+obj+": "+e.getMessage(),e);
                throw new WicketRuntimeException(e);
            }
        }
        public void flush() throws IOException{
            this.oos.flush();
        }
        public void close() throws IOException{
            this.oos.close();
        }
    }
}
