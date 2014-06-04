package org.apache.wicket.pageStore;

import org.apache.wicket.util.lang.*;
import java.util.concurrent.*;
import org.apache.wicket.*;
import org.apache.wicket.util.file.*;
import java.util.*;
import org.slf4j.*;
import java.nio.*;
import org.apache.wicket.util.io.*;
import java.nio.channels.*;
import java.io.*;

public class DiskDataStore implements IDataStore{
    private static final Logger log;
    private static final String INDEX_FILE_NAME="DiskDataStoreIndex";
    private final String applicationName;
    private final Bytes maxSizePerPageSession;
    private final File fileStoreFolder;
    private final ConcurrentMap<String,SessionEntry> sessionEntryMap;
    public DiskDataStore(final String applicationName,final File fileStoreFolder,final Bytes maxSizePerSession){
        super();
        this.applicationName=applicationName;
        this.fileStoreFolder=fileStoreFolder;
        this.maxSizePerPageSession=(Bytes)Args.notNull((Object)maxSizePerSession,"maxSizePerSession");
        this.sessionEntryMap=new ConcurrentHashMap<String,SessionEntry>();
        try{
            if(this.fileStoreFolder.exists()||this.fileStoreFolder.mkdirs()){
                this.loadIndex();
            }
            else{
                DiskDataStore.log.warn("Cannot create file store folder for some reason.");
            }
        }
        catch(SecurityException e){
            throw new WicketRuntimeException("SecurityException occurred while creating DiskDataStore. Consider using a non-disk based IDataStore implementation. See org.apache.wicket.Application.setPageManagerProvider(IPageManagerProvider)",e);
        }
    }
    public void destroy(){
        DiskDataStore.log.debug("Destroying...");
        this.saveIndex();
        DiskDataStore.log.debug("Destroyed.");
    }
    public byte[] getData(final String sessionId,final int id){
        byte[] pageData=null;
        final SessionEntry sessionEntry=this.getSessionEntry(sessionId,false);
        if(sessionEntry!=null){
            pageData=sessionEntry.loadPage(id);
        }
        DiskDataStore.log.debug("Returning data{} for page with id '{}' in session with id '{}'",(pageData!=null)?"":"(null)",id,sessionId);
        return pageData;
    }
    public boolean isReplicated(){
        return false;
    }
    public void removeData(final String sessionId,final int id){
        final SessionEntry sessionEntry=this.getSessionEntry(sessionId,false);
        if(sessionEntry!=null){
            DiskDataStore.log.debug("Removing data for page with id '{}' in session with id '{}'",id,sessionId);
            sessionEntry.removePage(id);
        }
    }
    public void removeData(final String sessionId){
        final SessionEntry sessionEntry=this.getSessionEntry(sessionId,false);
        if(sessionEntry!=null){
            DiskDataStore.log.debug("Removing data for pages in session with id '{}'",sessionId);
            synchronized(sessionEntry){
                this.sessionEntryMap.remove(sessionEntry.sessionId);
                sessionEntry.unbind();
            }
        }
    }
    public void storeData(final String sessionId,final int id,final byte[] data){
        final SessionEntry sessionEntry=this.getSessionEntry(sessionId,true);
        if(sessionEntry!=null){
            DiskDataStore.log.debug("Storing data for page with id '{}' in session with id '{}'",id,sessionId);
            sessionEntry.savePage(id,data);
        }
    }
    protected SessionEntry getSessionEntry(final String sessionId,final boolean create){
        if(!create){
            return (SessionEntry)this.sessionEntryMap.get(sessionId);
        }
        final SessionEntry entry=new SessionEntry(this,sessionId);
        final SessionEntry existing=this.sessionEntryMap.putIfAbsent(sessionId,entry);
        return (existing!=null)?existing:entry;
    }
    private void loadIndex(){
        final File storeFolder=this.getStoreFolder();
        final File index=new File(storeFolder,"DiskDataStoreIndex");
        if(index.exists()&&index.length()>0L){
            try{
                final InputStream stream=new FileInputStream(index);
                final ObjectInputStream ois=new ObjectInputStream(stream);
                final Map<String,SessionEntry> map=(Map<String,SessionEntry>)ois.readObject();
                this.sessionEntryMap.clear();
                this.sessionEntryMap.putAll(map);
                for(final Map.Entry<String,SessionEntry> entry : this.sessionEntryMap.entrySet()){
                    final SessionEntry sessionEntry=(SessionEntry)entry.getValue();
                    sessionEntry.diskDataStore=this;
                }
                stream.close();
            }
            catch(Exception e){
                DiskDataStore.log.error("Couldn't load DiskDataStore index from file "+index+".",e);
            }
        }
        Files.remove(index);
    }
    private void saveIndex(){
        final File storeFolder=this.getStoreFolder();
        if(storeFolder.exists()){
            final File index=new File(storeFolder,"DiskDataStoreIndex");
            Files.remove(index);
            try{
                final OutputStream stream=new FileOutputStream(index);
                final ObjectOutputStream oos=new ObjectOutputStream(stream);
                final Map<String,SessionEntry> map=(Map<String,SessionEntry>)new HashMap(this.sessionEntryMap.size());
                for(final Map.Entry<String,SessionEntry> e : this.sessionEntryMap.entrySet()){
                    if(!((SessionEntry)e.getValue()).unbound){
                        map.put(e.getKey(),e.getValue());
                    }
                }
                oos.writeObject(map);
                stream.close();
            }
            catch(Exception e2){
                DiskDataStore.log.error("Couldn't write DiskDataStore index to file "+index+".",e2);
            }
        }
    }
    private String getSessionFileName(final String sessionId,final boolean createSessionFolder){
        final File sessionFolder=this.getSessionFolder(sessionId,createSessionFolder);
        return new File(sessionFolder,"data").getAbsolutePath();
    }
    protected File getStoreFolder(){
        return new File(this.fileStoreFolder,this.applicationName+"-filestore");
    }
    protected File getSessionFolder(String sessionId,final boolean create){
        final File storeFolder=this.getStoreFolder();
        sessionId=sessionId.replace('*','_');
        sessionId=sessionId.replace('/','_');
        sessionId=sessionId.replace(':','_');
        sessionId=this.createPathFrom(sessionId);
        final File sessionFolder=new File(storeFolder,sessionId);
        if(create&&!sessionFolder.exists()){
            Files.mkdirs(sessionFolder);
        }
        return sessionFolder;
    }
    private String createPathFrom(final String sessionId){
        final int hash=Math.abs(sessionId.hashCode());
        final String low=String.valueOf(hash%9973);
        final String high=String.valueOf(hash/9973%9973);
        final StringBuilder bs=new StringBuilder(sessionId.length()+10);
        bs.append(low);
        bs.append(File.separator);
        bs.append(high);
        bs.append(File.separator);
        bs.append(sessionId);
        return bs.toString();
    }
    static{
        log=LoggerFactory.getLogger(DiskDataStore.class);
    }
    protected static class SessionEntry implements Serializable{
        private static final long serialVersionUID=1L;
        private final String sessionId;
        private transient DiskDataStore diskDataStore;
        private String fileName;
        private PageWindowManager manager;
        private boolean unbound;
        protected SessionEntry(final DiskDataStore diskDataStore,final String sessionId){
            super();
            this.unbound=false;
            this.diskDataStore=diskDataStore;
            this.sessionId=sessionId;
        }
        public PageWindowManager getManager(){
            if(this.manager==null){
                this.manager=new PageWindowManager(this.diskDataStore.maxSizePerPageSession.bytes());
            }
            return this.manager;
        }
        private String getFileName(){
            if(this.fileName==null){
                this.fileName=this.diskDataStore.getSessionFileName(this.sessionId,true);
            }
            return this.fileName;
        }
        public String getSessionId(){
            return this.sessionId;
        }
        public synchronized void savePage(final int pageId,final byte[] data){
            if(this.unbound){
                return;
            }
            if(data!=null){
                final PageWindowManager.PageWindow window=this.getManager().createPageWindow(pageId,data.length);
                final FileChannel channel=this.getFileChannel(true);
                if(channel!=null){
                    try{
                        channel.write(ByteBuffer.wrap(data),window.getFilePartOffset());
                    }
                    catch(IOException e){
                        DiskDataStore.log.error("Error writing to a channel "+channel,e);
                    }
                    finally{
                        IOUtils.closeQuietly((Closeable)channel);
                    }
                }
                else{
                    DiskDataStore.log.warn("Cannot save page with id '{}' because the data file cannot be opened.",(Object)pageId);
                }
            }
        }
        public synchronized void removePage(final int pageId){
            if(this.unbound){
                return;
            }
            this.getManager().removePage(pageId);
        }
        public byte[] loadPage(final PageWindowManager.PageWindow window){
            byte[] result=null;
            final FileChannel channel=this.getFileChannel(false);
            if(channel!=null){
                final ByteBuffer buffer=ByteBuffer.allocate(window.getFilePartSize());
                try{
                    channel.read(buffer,window.getFilePartOffset());
                    if(buffer.hasArray()){
                        result=buffer.array();
                    }
                }
                catch(IOException e){
                    DiskDataStore.log.error("Error reading from file channel "+channel,e);
                }
                finally{
                    IOUtils.closeQuietly((Closeable)channel);
                }
            }
            return result;
        }
        private FileChannel getFileChannel(final boolean create){
            FileChannel channel=null;
            final File file=new File(this.getFileName());
            if(create||file.exists()){
                final String mode=create?"rw":"r";
                try{
                    final RandomAccessFile randomAccessFile=new RandomAccessFile(file,mode);
                    channel=randomAccessFile.getChannel();
                }
                catch(FileNotFoundException fnfx){
                    DiskDataStore.log.error(fnfx.getMessage(),fnfx);
                }
            }
            return channel;
        }
        public synchronized byte[] loadPage(final int id){
            if(this.unbound){
                return null;
            }
            byte[] result=null;
            final PageWindowManager.PageWindow window=this.getManager().getPageWindow(id);
            if(window!=null){
                result=this.loadPage(window);
            }
            return result;
        }
        public synchronized void unbind(){
            final File sessionFolder=this.diskDataStore.getSessionFolder(this.sessionId,false);
            if(sessionFolder.exists()){
                Files.removeFolder(sessionFolder);
                this.cleanup(sessionFolder);
            }
            this.unbound=true;
        }
        private void cleanup(final File sessionFolder){
            final File high=sessionFolder.getParentFile();
            if(high.list().length==0&&Files.removeFolder(high)){
                final File low=high.getParentFile();
                if(low.list().length==0){
                    Files.removeFolder(low);
                }
            }
        }
    }
}
