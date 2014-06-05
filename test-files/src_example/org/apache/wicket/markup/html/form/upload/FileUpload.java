package org.apache.wicket.markup.html.form.upload;

import org.apache.wicket.util.upload.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.io.*;
import java.security.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import java.io.*;
import org.apache.wicket.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.file.*;

public class FileUpload implements IClusterable{
    private static final long serialVersionUID=1L;
    private final FileItem item;
    private transient List<InputStream> inputStreamsToClose;
    public FileUpload(final FileItem item){
        super();
        Args.notNull((Object)item,"item");
        this.item=item;
    }
    public final void closeStreams(){
        if(this.inputStreamsToClose!=null){
            for(final InputStream inputStream : this.inputStreamsToClose){
                IOUtils.closeQuietly((Closeable)inputStream);
            }
            this.inputStreamsToClose=null;
        }
    }
    public void delete(){
        this.item.delete();
    }
    public byte[] getBytes(){
        return this.item.get();
    }
    public byte[] getDigest(final String algorithm){
        try{
            Args.notEmpty((CharSequence)algorithm,"algorithm");
            final MessageDigest digest=MessageDigest.getInstance(algorithm);
            if(this.item.isInMemory()){
                digest.update(this.getBytes());
                return digest.digest();
            }
            InputStream in=null;
            try{
                in=this.item.getInputStream();
                final byte[] buf=new byte[Math.min((int)this.item.getSize(),40960)];
                int len;
                while(-1!=(len=in.read(buf))){
                    digest.update(buf,0,len);
                }
                return digest.digest();
            }
            catch(IOException ex){
                throw new WicketRuntimeException("Error while reading input data for "+algorithm+" checksum",ex);
            }
            finally{
                IOUtils.closeQuietly((Closeable)in);
            }
        }
        catch(NoSuchAlgorithmException ex2){
            final String error=String.format("Your java runtime does not support digest algorithm [%s]. Please see java.security.MessageDigest.getInstance(\"%s\")",new Object[] { algorithm,algorithm });
            throw new WicketRuntimeException(error,ex2);
        }
    }
    public byte[] getMD5(){
        return this.getDigest("MD5");
    }
    public String getClientFileName(){
        String name=this.item.getName();
        name=Strings.lastPathComponent(name,'/');
        name=Strings.lastPathComponent(name,'\\');
        return name;
    }
    public String getContentType(){
        return this.item.getContentType();
    }
    public InputStream getInputStream() throws IOException{
        if(this.inputStreamsToClose==null){
            this.inputStreamsToClose=(List<InputStream>)new ArrayList();
        }
        final InputStream is=this.item.getInputStream();
        this.inputStreamsToClose.add(is);
        return is;
    }
    public long getSize(){
        return this.item.getSize();
    }
    public void writeTo(final File file) throws IOException{
        this.item.write(file);
    }
    public final File writeToTempFile() throws IOException{
        final String sessionId=Session.exists()?Session.get().getId():"";
        final String tempFileName=sessionId+"_"+RequestCycle.get().getStartTime();
        final File temp=File.createTempFile(tempFileName,Files.cleanupFilename(this.item.getFieldName()));
        this.writeTo(temp);
        return temp;
    }
}
