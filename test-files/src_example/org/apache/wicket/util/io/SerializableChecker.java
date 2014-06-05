package org.apache.wicket.util.io;

import org.apache.wicket.*;
import org.apache.wicket.util.objects.checker.*;
import java.io.*;
import java.lang.reflect.*;

public class SerializableChecker extends CheckingObjectOutputStream{
    public SerializableChecker(final NotSerializableException exception) throws IOException{
        this((OutputStream)new ByteArrayOutputStream(),exception);
    }
    public SerializableChecker(final OutputStream outputStream,final NotSerializableException exception) throws IOException{
        super(outputStream,new IObjectChecker[] { new ObjectSerializationChecker(exception) });
    }
    @Deprecated
    public static boolean isAvailable(){
        return CheckingObjectOutputStream.isAvailable();
    }
    @Deprecated
    public static final class WicketNotSerializableException extends WicketRuntimeException{
        private static final long serialVersionUID=1L;
        private WicketNotSerializableException(final String message,final Throwable cause){
            super(message,cause);
        }
    }
    public static class ObjectSerializationChecker extends AbstractObjectChecker{
        private final NotSerializableException cause;
        public ObjectSerializationChecker(){
            this((NotSerializableException)null);
        }
        public ObjectSerializationChecker(final NotSerializableException cause){
            super();
            this.cause=cause;
        }
        public IObjectChecker.Result check(final Object object){
            IObjectChecker.Result result=IObjectChecker.Result.SUCCESS;
            if(!(object instanceof Serializable)&&!Proxy.isProxyClass(object.getClass())){
                result=new IObjectChecker.Result(IObjectChecker.Result.Status.FAILURE,"The object type is not Serializable!",this.cause);
            }
            return result;
        }
    }
}
