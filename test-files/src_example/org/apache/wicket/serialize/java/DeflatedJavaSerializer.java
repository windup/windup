package org.apache.wicket.serialize.java;

import java.io.*;
import java.util.zip.*;

public class DeflatedJavaSerializer extends JavaSerializer{
    private static final int COMPRESS_BUF_SIZE=4096;
    public DeflatedJavaSerializer(final String applicationKey){
        super(applicationKey);
    }
    protected ObjectOutputStream newObjectOutputStream(final OutputStream out) throws IOException{
        return super.newObjectOutputStream(new DeflaterOutputStream(out,this.createDeflater(),4096));
    }
    protected Deflater createDeflater(){
        return new Deflater(1);
    }
    protected ObjectInputStream newObjectInputStream(final InputStream in) throws IOException{
        return super.newObjectInputStream(new InflaterInputStream(in,new Inflater(),4096));
    }
}
