package org.apache.commons.lang;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.commons.lang.SerializationException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class SerializationUtils{
    public static Object clone(final Serializable object){
        return deserialize(serialize(object));
    }
    public static void serialize(final Serializable obj,final OutputStream outputStream){
        if(outputStream==null){
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream out=null;
        try{
            out=new ObjectOutputStream(outputStream);
            out.writeObject(obj);
        }
        catch(IOException ex){
            throw new SerializationException(ex);
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
            }
            catch(IOException ex2){
            }
        }
    }
    public static byte[] serialize(final Serializable obj){
        final ByteArrayOutputStream baos=new ByteArrayOutputStream(512);
        serialize(obj,baos);
        return baos.toByteArray();
    }
    public static Object deserialize(final InputStream inputStream){
        if(inputStream==null){
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream in=null;
        try{
            in=new ObjectInputStream(inputStream);
            return in.readObject();
        }
        catch(ClassNotFoundException ex){
            throw new SerializationException(ex);
        }
        catch(IOException ex2){
            throw new SerializationException(ex2);
        }
        finally{
            try{
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex3){
            }
        }
    }
    public static Object deserialize(final byte[] objectData){
        if(objectData==null){
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        final ByteArrayInputStream bais=new ByteArrayInputStream(objectData);
        return deserialize(bais);
    }
}
