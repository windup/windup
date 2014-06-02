package org.apache.wicket.markup.html.form.upload;

import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.util.upload.*;
import org.apache.wicket.request.*;
import java.util.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.markup.*;

public class FileUploadField extends FormComponent<List<FileUpload>>{
    private static final long serialVersionUID=1L;
    private transient List<FileUpload> fileUploads;
    public FileUploadField(final String id){
        super(id);
    }
    public FileUploadField(final String id,final IModel<List<FileUpload>> model){
        super(id,model);
    }
    public FileUpload getFileUpload(){
        final List<FileUpload> fileUploads=this.getFileUploads();
        return (fileUploads!=null&&!fileUploads.isEmpty())?((FileUpload)fileUploads.get(0)):null;
    }
    public List<FileUpload> getFileUploads(){
        if(this.fileUploads!=null){
            return this.fileUploads;
        }
        final Request request=this.getRequest();
        if(request instanceof IMultipartWebRequest){
            final List<FileItem> fileItems=((IMultipartWebRequest)request).getFile(this.getInputName());
            if(fileItems!=null){
                for(final FileItem item : fileItems){
                    if(item!=null&&item.getSize()>0L){
                        if(this.fileUploads==null){
                            this.fileUploads=(List<FileUpload>)new ArrayList();
                        }
                        this.fileUploads.add(new FileUpload(item));
                    }
                }
            }
        }
        return this.fileUploads;
    }
    public void updateModel(){
        if(this.getModel()!=null){
            super.updateModel();
        }
    }
    public String[] getInputAsArray(){
        final List<FileUpload> fileUploads=this.getFileUploads();
        if(fileUploads!=null){
            final List<String> clientFileNames=(List<String>)new ArrayList();
            for(final FileUpload fu : fileUploads){
                clientFileNames.add(fu.getClientFileName());
            }
            return (String[])clientFileNames.toArray(new String[clientFileNames.size()]);
        }
        return null;
    }
    protected List<FileUpload> convertValue(final String[] value) throws ConversionException{
        final String[] filenames=this.getInputAsArray();
        if(filenames==null){
            return null;
        }
        return this.getFileUploads();
    }
    public boolean isMultiPart(){
        return true;
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"input");
        this.checkComponentTagAttribute(tag,"type","file");
        super.onComponentTag(tag);
    }
    protected void onDetach(){
        if(this.fileUploads!=null&&this.forceCloseStreamsOnDetach()){
            for(final FileUpload fu : this.fileUploads){
                fu.closeStreams();
            }
            this.fileUploads=null;
            if(this.getModel()!=null){
                this.getModel().setObject(null);
            }
        }
        super.onDetach();
    }
    protected boolean forceCloseStreamsOnDetach(){
        return true;
    }
}
