package org.apache.wicket.request.resource.caching.version;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.util.resource.*;
import java.io.*;
import org.apache.wicket.*;
import java.security.*;
import org.slf4j.*;

public class MessageDigestResourceVersion implements IResourceVersion{
    private static final Logger log;
    private static final String DEFAULT_ALGORITHM="MD5";
    private static final int DEFAULT_BUFFER_BYTES=8192;
    private static final Bytes DEFAULT_BUFFER_SIZE;
    private final String algorithm;
    private final Bytes bufferSize;
    public MessageDigestResourceVersion(){
        this("MD5",MessageDigestResourceVersion.DEFAULT_BUFFER_SIZE);
    }
    public MessageDigestResourceVersion(final String algorithm){
        this(algorithm,MessageDigestResourceVersion.DEFAULT_BUFFER_SIZE);
    }
    public MessageDigestResourceVersion(final String algorithm,final Bytes bufferSize){
        super();
        this.algorithm=(String)Args.notEmpty((CharSequence)algorithm,"algorithm");
        this.bufferSize=(Bytes)Args.notNull((Object)bufferSize,"bufferSize");
    }
    public String getVersion(final IStaticCacheableResource resource){
        final IResourceStream stream=resource.getCacheableResourceStream();
        if(stream==null){
            return null;
        }
        try{
            final InputStream inputStream=stream.getInputStream();
            try{
                final byte[] hash=this.computeDigest(inputStream);
                return Strings.toHexString(hash);
            }
            finally{
                IOUtils.close((Closeable)stream);
            }
        }
        catch(IOException e){
            MessageDigestResourceVersion.log.warn("unable to compute hash for "+resource,e);
            return null;
        }
        catch(ResourceStreamNotFoundException e2){
            MessageDigestResourceVersion.log.warn("unable to locate resource for "+resource,(Throwable)e2);
            return null;
        }
    }
    protected MessageDigest getMessageDigest(){
        try{
            return MessageDigest.getInstance(this.algorithm);
        }
        catch(NoSuchAlgorithmException e){
            throw new WicketRuntimeException("message digest "+this.algorithm+" not found",e);
        }
    }
    protected byte[] computeDigest(final InputStream inputStream) throws IOException{
        final MessageDigest digest=this.getMessageDigest();
        final int bufferLen=(int)Math.min(2147483647L,this.bufferSize.bytes());
        final byte[] buf=new byte[bufferLen];
        int len;
        while((len=inputStream.read(buf))!=-1){
            digest.update(buf,0,len);
        }
        return digest.digest();
    }
    static{
        log=LoggerFactory.getLogger(MessageDigestResourceVersion.class);
        DEFAULT_BUFFER_SIZE=Bytes.bytes(8192L);
    }
}
