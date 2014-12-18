package org.apache.log4j.lf5.viewer.configure;

import java.util.Iterator;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.LinkedList;

public class MRUFileManager{
    private static final String CONFIG_FILE_NAME="mru_file_manager";
    private static final int DEFAULT_MAX_SIZE=3;
    private int _maxSize;
    private LinkedList _mruFileList;
    public MRUFileManager(){
        super();
        this._maxSize=0;
        this.load();
        this.setMaxSize(3);
    }
    public MRUFileManager(final int maxSize){
        super();
        this._maxSize=0;
        this.load();
        this.setMaxSize(maxSize);
    }
    public void save(){
        final File file=new File(this.getFilename());
        try{
            final ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(this._mruFileList);
            oos.flush();
            oos.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public int size(){
        return this._mruFileList.size();
    }
    public Object getFile(final int index){
        if(index<this.size()){
            return this._mruFileList.get(index);
        }
        return null;
    }
    public InputStream getInputStream(final int index) throws IOException,FileNotFoundException{
        if(index>=this.size()){
            return null;
        }
        final Object o=this.getFile(index);
        if(o instanceof File){
            return this.getInputStream((File)o);
        }
        return this.getInputStream((URL)o);
    }
    public void set(final File file){
        this.setMRU(file);
    }
    public void set(final URL url){
        this.setMRU(url);
    }
    public String[] getMRUFileList(){
        if(this.size()==0){
            return null;
        }
        final String[] ss=new String[this.size()];
        for(int i=0;i<this.size();++i){
            final Object o=this.getFile(i);
            if(o instanceof File){
                ss[i]=((File)o).getAbsolutePath();
            }
            else{
                ss[i]=o.toString();
            }
        }
        return ss;
    }
    public void moveToTop(final int index){
        this._mruFileList.add(0,this._mruFileList.remove(index));
    }
    public static void createConfigurationDirectory(){
        final String home=System.getProperty("user.home");
        final String sep=System.getProperty("file.separator");
        final File f=new File(home+sep+"lf5");
        if(!f.exists()){
            try{
                f.mkdir();
            }
            catch(SecurityException e){
                e.printStackTrace();
            }
        }
    }
    protected InputStream getInputStream(final File file) throws IOException,FileNotFoundException{
        final BufferedInputStream reader=new BufferedInputStream(new FileInputStream(file));
        return reader;
    }
    protected InputStream getInputStream(final URL url) throws IOException{
        return url.openStream();
    }
    protected void setMRU(final Object o){
        final int index=this._mruFileList.indexOf(o);
        if(index==-1){
            this._mruFileList.add(0,o);
            this.setMaxSize(this._maxSize);
        }
        else{
            this.moveToTop(index);
        }
    }
    protected void load(){
        createConfigurationDirectory();
        final File file=new File(this.getFilename());
        if(file.exists()){
            try{
                final ObjectInputStream ois=new ObjectInputStream(new FileInputStream(file));
                this._mruFileList=(LinkedList)ois.readObject();
                ois.close();
                final Iterator it=this._mruFileList.iterator();
                while(it.hasNext()){
                    final Object o=it.next();
                    if(!(o instanceof File)&&!(o instanceof URL)){
                        it.remove();
                    }
                }
            }
            catch(Exception e){
                this._mruFileList=new LinkedList();
            }
        }
        else{
            this._mruFileList=new LinkedList();
        }
    }
    protected String getFilename(){
        final String home=System.getProperty("user.home");
        final String sep=System.getProperty("file.separator");
        return home+sep+"lf5"+sep+"mru_file_manager";
    }
    protected void setMaxSize(final int maxSize){
        if(maxSize<this._mruFileList.size()){
            for(int i=0;i<this._mruFileList.size()-maxSize;++i){
                this._mruFileList.removeLast();
            }
        }
        this._maxSize=maxSize;
    }
}
