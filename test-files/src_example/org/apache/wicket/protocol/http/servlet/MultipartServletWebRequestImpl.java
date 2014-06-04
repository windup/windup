package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.util.value.*;
import javax.servlet.http.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import java.io.*;
import java.util.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.upload.*;

public class MultipartServletWebRequestImpl extends MultipartServletWebRequest{
    private final Map<String,List<FileItem>> files;
    private final ValueMap parameters;
    private final String upload;
    private int bytesUploaded;
    private int totalBytes;
    private static final String SESSION_KEY;
    public MultipartServletWebRequestImpl(final HttpServletRequest request,final String filterPrefix,final Bytes maxSize,final String upload) throws FileUploadException{
        this(request,filterPrefix,maxSize,upload,(FileItemFactory)new DiskFileItemFactory(Application.get().getResourceSettings().getFileCleaner()));
    }
    public MultipartServletWebRequestImpl(final HttpServletRequest request,final String filterPrefix,final Bytes maxSize,final String upload,final FileItemFactory factory) throws FileUploadException{
        super(request,filterPrefix);
        Args.notNull((Object)maxSize,"maxSize");
        Args.notNull((Object)upload,"upload");
        this.upload=upload;
        this.parameters=new ValueMap();
        this.files=(Map<String,List<FileItem>>)new HashMap();
        final boolean isMultipart=ServletFileUpload.isMultipartContent(request);
        if(!isMultipart){
            throw new IllegalStateException("ServletRequest does not contain multipart content. One possible solution is to explicitly call Form.setMultipart(true), Wicket tries its best to auto-detect multipart forms but there are certain situation where it cannot.");
        }
        final ServletFileUpload fileUpload=new ServletFileUpload(factory);
        String encoding=request.getCharacterEncoding();
        if(encoding==null){
            encoding=Application.get().getRequestCycleSettings().getResponseRequestEncoding();
        }
        if(encoding!=null){
            fileUpload.setHeaderEncoding(encoding);
        }
        fileUpload.setSizeMax(maxSize.bytes());
        List<FileItem> items;
        if(this.wantUploadProgressUpdates()){
            final ServletRequestContext ctx=new ServletRequestContext(request){
                public InputStream getInputStream() throws IOException{
                    return new CountingInputStream(super.getInputStream());
                }
            };
            this.onUploadStarted(this.totalBytes=request.getContentLength());
            try{
                items=(List<FileItem>)fileUpload.parseRequest((RequestContext)ctx);
            }
            finally{
                this.onUploadCompleted();
            }
        }
        else{
            items=(List<FileItem>)fileUpload.parseRequest(request);
        }
        for(final FileItem item : items){
            if(item.isFormField()){
                String value=null;
                Label_0281:{
                    if(encoding!=null){
                        try{
                            value=item.getString(encoding);
                            break Label_0281;
                        }
                        catch(UnsupportedEncodingException e){
                            throw new WicketRuntimeException(e);
                        }
                    }
                    value=item.getString();
                }
                this.addParameter(item.getFieldName(),value);
            }
            else{
                List<FileItem> fileItems=(List<FileItem>)this.files.get(item.getFieldName());
                if(fileItems==null){
                    fileItems=(List<FileItem>)new ArrayList();
                    this.files.put(item.getFieldName(),fileItems);
                }
                fileItems.add(item);
            }
        }
    }
    private void addParameter(final String name,final String value){
        final String[] currVal=(String[])this.parameters.get((Object)name);
        String[] newVal=null;
        if(currVal!=null){
            newVal=new String[currVal.length+1];
            System.arraycopy(currVal,0,newVal,0,currVal.length);
            newVal[currVal.length]=value;
        }
        else{
            newVal=new String[] { value };
        }
        this.parameters.put(name,(Object)newVal);
    }
    public Map<String,List<FileItem>> getFiles(){
        return this.files;
    }
    public List<FileItem> getFile(final String fieldName){
        return (List<FileItem>)this.files.get(fieldName);
    }
    protected Map<String,List<StringValue>> generatePostParameters(){
        final Map<String,List<StringValue>> res=(Map<String,List<StringValue>>)new HashMap();
        for(final String key : this.parameters.keySet()){
            final String[] val=(String[])this.parameters.get((Object)key);
            if(val!=null&&val.length>0){
                final List<StringValue> items=(List<StringValue>)new ArrayList();
                for(final String s : val){
                    items.add(StringValue.valueOf(s));
                }
                res.put(key,items);
            }
        }
        return res;
    }
    protected boolean wantUploadProgressUpdates(){
        return Application.get().getApplicationSettings().isUploadProgressUpdatesEnabled();
    }
    protected void onUploadStarted(final int totalBytes){
        final UploadInfo info=new UploadInfo(totalBytes);
        setUploadInfo(this.getContainerRequest(),this.upload,info);
    }
    protected void onUploadUpdate(final int bytesUploaded,final int total){
        final HttpServletRequest request=this.getContainerRequest();
        final UploadInfo info=getUploadInfo(request,this.upload);
        if(info==null){
            throw new IllegalStateException("could not find UploadInfo object in session which should have been set when uploaded started");
        }
        info.setBytesUploaded(bytesUploaded);
        setUploadInfo(request,this.upload,info);
    }
    protected void onUploadCompleted(){
        clearUploadInfo(this.getContainerRequest(),this.upload);
    }
    public MultipartServletWebRequest newMultipartWebRequest(final Bytes maxSize,final String upload) throws FileUploadException{
        for(final Map.Entry<String,List<FileItem>> entry : this.files.entrySet()){
            final List<FileItem> fileItems=(List<FileItem>)entry.getValue();
            for(final FileItem fileItem : fileItems){
                if(fileItem.getSize()>maxSize.bytes()){
                    final String fieldName=(String)entry.getKey();
                    final FileUploadException fslex=(FileUploadException)new FileUploadBase.FileSizeLimitExceededException("The field "+fieldName+" exceeds its maximum permitted "+" size of "+maxSize+" characters.",fileItem.getSize(),maxSize.bytes());
                    throw fslex;
                }
            }
        }
        return this;
    }
    public MultipartServletWebRequest newMultipartWebRequest(final Bytes maxSize,final String upload,final FileItemFactory factory) throws FileUploadException{
        return this;
    }
    private static String getSessionKey(final String upload){
        return MultipartServletWebRequestImpl.SESSION_KEY+":"+upload;
    }
    public static UploadInfo getUploadInfo(final HttpServletRequest req,final String upload){
        Args.notNull((Object)req,"req");
        return (UploadInfo)req.getSession().getAttribute(getSessionKey(upload));
    }
    public static void setUploadInfo(final HttpServletRequest req,final String upload,final UploadInfo uploadInfo){
        Args.notNull((Object)req,"req");
        Args.notNull((Object)upload,"upload");
        Args.notNull((Object)uploadInfo,"uploadInfo");
        req.getSession().setAttribute(getSessionKey(upload),uploadInfo);
    }
    public static void clearUploadInfo(final HttpServletRequest req,final String upload){
        Args.notNull((Object)req,"req");
        Args.notNull((Object)upload,"upload");
        req.getSession().removeAttribute(getSessionKey(upload));
    }
    static{
        SESSION_KEY=MultipartServletWebRequestImpl.class.getName();
    }
    private class CountingInputStream extends InputStream{
        private final InputStream in;
        public CountingInputStream(final InputStream in){
            super();
            this.in=in;
        }
        public int read() throws IOException{
            final int read=this.in.read();
            MultipartServletWebRequestImpl.this.bytesUploaded+=((read>=0)?1:0);
            MultipartServletWebRequestImpl.this.onUploadUpdate(MultipartServletWebRequestImpl.this.bytesUploaded,MultipartServletWebRequestImpl.this.totalBytes);
            return read;
        }
        public int read(final byte[] b) throws IOException{
            final int read=this.in.read(b);
            MultipartServletWebRequestImpl.this.bytesUploaded+=((read<0)?0:read);
            MultipartServletWebRequestImpl.this.onUploadUpdate(MultipartServletWebRequestImpl.this.bytesUploaded,MultipartServletWebRequestImpl.this.totalBytes);
            return read;
        }
        public int read(final byte[] b,final int off,final int len) throws IOException{
            final int read=this.in.read(b,off,len);
            MultipartServletWebRequestImpl.this.bytesUploaded+=((read<0)?0:read);
            MultipartServletWebRequestImpl.this.onUploadUpdate(MultipartServletWebRequestImpl.this.bytesUploaded,MultipartServletWebRequestImpl.this.totalBytes);
            return read;
        }
    }
}
