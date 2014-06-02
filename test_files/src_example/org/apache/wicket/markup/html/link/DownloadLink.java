package org.apache.wicket.markup.html.link;

import org.apache.wicket.util.time.*;
import java.io.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.file.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.*;

public class DownloadLink extends Link<File>{
    private static final long serialVersionUID=1L;
    private IModel<String> fileNameModel;
    private boolean deleteAfter;
    private Duration cacheDuration;
    public DownloadLink(final String id,final File file){
        this(id,new Model<File>((File)Args.notNull((Object)file,"file")));
    }
    public DownloadLink(final String id,final IModel<File> model){
        this(id,model,(IModel<String>)null);
    }
    public DownloadLink(final String id,final IModel<File> model,final String fileName){
        this(id,model,Model.of(fileName));
    }
    public DownloadLink(final String id,final File file,final String fileName){
        this(id,(IModel<File>)Model.of(Args.notNull((Object)file,"file")),Model.of(fileName));
    }
    public DownloadLink(final String id,final IModel<File> fileModel,final IModel<String> fileNameModel){
        super(id,fileModel);
        this.fileNameModel=this.wrap(fileNameModel);
    }
    public void detachModels(){
        super.detachModels();
        if(this.fileNameModel!=null){
            this.fileNameModel.detach();
        }
    }
    public void onClick(){
        final File file=this.getModelObject();
        if(file==null){
            throw new IllegalStateException(this.getClass().getName()+" failed to retrieve a File object from model");
        }
        String fileName=(this.fileNameModel!=null)?this.fileNameModel.getObject():null;
        if(Strings.isEmpty((CharSequence)fileName)){
            fileName=file.getName();
        }
        fileName=UrlEncoder.QUERY_INSTANCE.encode(fileName,this.getRequest().getCharset());
        final IResourceStream resourceStream=(IResourceStream)new FileResourceStream(new org.apache.wicket.util.file.File(file));
        this.getRequestCycle().scheduleRequestHandlerAfterCurrent((IRequestHandler)new ResourceStreamRequestHandler(resourceStream){
            public void respond(final IRequestCycle requestCycle){
                super.respond(requestCycle);
                if(DownloadLink.this.deleteAfter){
                    Files.remove(file);
                }
            }
        }.setFileName(fileName).setContentDisposition(ContentDisposition.ATTACHMENT).setCacheDuration(this.cacheDuration));
    }
    public final DownloadLink setDeleteAfterDownload(final boolean deleteAfter){
        this.deleteAfter=deleteAfter;
        return this;
    }
    public DownloadLink setCacheDuration(final Duration duration){
        this.cacheDuration=duration;
        return this;
    }
}
