package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import java.lang.ref.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.application.*;

public class ApplicationSettings implements IApplicationSettings{
    private WeakReference<Class<? extends Page>> accessDeniedPage;
    private IClassResolver classResolver;
    private WeakReference<Class<? extends Page>> internalErrorPage;
    private WeakReference<Class<? extends Page>> pageExpiredErrorPage;
    private Bytes defaultMaximumUploadSize;
    private boolean uploadProgressUpdatesEnabled;
    public ApplicationSettings(){
        super();
        this.classResolver=new DefaultClassResolver();
        this.defaultMaximumUploadSize=Bytes.MAX;
        this.uploadProgressUpdatesEnabled=false;
    }
    public Class<? extends Page> getAccessDeniedPage(){
        return (Class<? extends Page>)this.accessDeniedPage.get();
    }
    public IClassResolver getClassResolver(){
        return this.classResolver;
    }
    public Bytes getDefaultMaximumUploadSize(){
        return this.defaultMaximumUploadSize;
    }
    public Class<? extends Page> getInternalErrorPage(){
        return (Class<? extends Page>)this.internalErrorPage.get();
    }
    public Class<? extends Page> getPageExpiredErrorPage(){
        return (Class<? extends Page>)this.pageExpiredErrorPage.get();
    }
    public boolean isUploadProgressUpdatesEnabled(){
        return this.uploadProgressUpdatesEnabled;
    }
    public void setAccessDeniedPage(final Class<? extends Page> accessDeniedPage){
        if(accessDeniedPage==null){
            throw new IllegalArgumentException("Argument accessDeniedPage may not be null");
        }
        this.checkPageClass(accessDeniedPage);
        this.accessDeniedPage=(WeakReference<Class<? extends Page>>)new WeakReference(accessDeniedPage);
    }
    public void setClassResolver(final IClassResolver defaultClassResolver){
        this.classResolver=defaultClassResolver;
    }
    public void setDefaultMaximumUploadSize(final Bytes defaultMaximumUploadSize){
        this.defaultMaximumUploadSize=defaultMaximumUploadSize;
    }
    public void setInternalErrorPage(final Class<? extends Page> internalErrorPage){
        if(internalErrorPage==null){
            throw new IllegalArgumentException("Argument internalErrorPage may not be null");
        }
        this.checkPageClass(internalErrorPage);
        this.internalErrorPage=(WeakReference<Class<? extends Page>>)new WeakReference(internalErrorPage);
    }
    public void setPageExpiredErrorPage(final Class<? extends Page> pageExpiredErrorPage){
        if(pageExpiredErrorPage==null){
            throw new IllegalArgumentException("Argument pageExpiredErrorPage may not be null");
        }
        this.checkPageClass(pageExpiredErrorPage);
        this.pageExpiredErrorPage=(WeakReference<Class<? extends Page>>)new WeakReference(pageExpiredErrorPage);
    }
    public void setUploadProgressUpdatesEnabled(final boolean uploadProgressUpdatesEnabled){
        this.uploadProgressUpdatesEnabled=uploadProgressUpdatesEnabled;
    }
    private <C extends Page> void checkPageClass(final Class<C> pageClass){
        if(!Page.class.isAssignableFrom(pageClass)){
            throw new IllegalArgumentException("argument "+pageClass+" must be a subclass of Page");
        }
    }
}
