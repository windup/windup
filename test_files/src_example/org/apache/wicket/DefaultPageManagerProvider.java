package org.apache.wicket;

import org.apache.wicket.page.*;
import org.apache.wicket.settings.*;
import org.apache.wicket.pageStore.*;
import org.apache.wicket.serialize.*;
import org.apache.wicket.util.lang.*;
import java.io.*;

public class DefaultPageManagerProvider implements IPageManagerProvider{
    protected final Application application;
    public DefaultPageManagerProvider(final Application application){
        super();
        this.application=application;
    }
    public IPageManager get(final IPageManagerContext pageManagerContext){
        IDataStore dataStore=this.newDataStore();
        final IStoreSettings storeSettings=this.getStoreSettings();
        if(storeSettings.isAsynchronous()&&dataStore instanceof DiskDataStore){
            final int capacity=storeSettings.getAsynchronousQueueCapacity();
            dataStore=new AsynchronousDataStore(dataStore,capacity);
        }
        final IPageStore pageStore=this.newPageStore(dataStore);
        return new PageStoreManager(this.application.getName(),pageStore,pageManagerContext);
    }
    protected IPageStore newPageStore(final IDataStore dataStore){
        final int inmemoryCacheSize=this.getStoreSettings().getInmemoryCacheSize();
        final ISerializer pageSerializer=this.application.getFrameworkSettings().getSerializer();
        return new DefaultPageStore(pageSerializer,dataStore,inmemoryCacheSize);
    }
    protected IDataStore newDataStore(){
        final IStoreSettings storeSettings=this.getStoreSettings();
        final Bytes maxSizePerSession=storeSettings.getMaxSizePerSession();
        final File fileStoreFolder=storeSettings.getFileStoreFolder();
        return new DiskDataStore(this.application.getName(),fileStoreFolder,maxSizePerSession);
    }
    IStoreSettings getStoreSettings(){
        return this.application.getStoreSettings();
    }
}
