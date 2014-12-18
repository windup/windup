package org.apache.log4j.lf5.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public abstract class StreamUtils{
    public static final int DEFAULT_BUFFER_SIZE=2048;
    public static void copy(final InputStream input,final OutputStream output) throws IOException{
        copy(input,output,2048);
    }
    public static void copy(final InputStream input,final OutputStream output,final int bufferSize) throws IOException{
        final byte[] buf=new byte[bufferSize];
        for(int bytesRead=input.read(buf);bytesRead!=-1;bytesRead=input.read(buf)){
            output.write(buf,0,bytesRead);
        }
        output.flush();
    }
    public static void copyThenClose(final InputStream input,final OutputStream output) throws IOException{
        copy(input,output);
        input.close();
        output.close();
    }
    public static byte[] getBytes(final InputStream input) throws IOException{
        final ByteArrayOutputStream result=new ByteArrayOutputStream();
        copy(input,result);
        result.close();
        return result.toByteArray();
    }
}
