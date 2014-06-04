package org.apache.wicket.util.file;

import javax.servlet.*;
import org.apache.wicket.util.resource.*;
import java.net.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import org.slf4j.*;

public final class WebApplicationPath implements IResourcePath{
    private static final Logger log;
    private static final String WEB_INF="WEB-INF/";
    private final List<String> webappPaths;
    private final List<Folder> folders;
    private final ServletContext servletContext;
    public WebApplicationPath(final ServletContext servletContext){
        super();
        this.webappPaths=(List<String>)new ArrayList();
        this.folders=(List<Folder>)new ArrayList();
        this.servletContext=servletContext;
        this.webappPaths.add("/");
    }
    public void add(String path){
        final Folder folder=new Folder(path);
        if(folder.exists()){
            this.folders.add(folder);
        }
        else{
            if(!path.startsWith("/")){
                path="/"+path;
            }
            if(!path.endsWith("/")){
                path+="/";
            }
            this.webappPaths.add(path);
        }
    }
    public IResourceStream find(final Class<?> clazz,final String pathname){
        for(final Folder folder : this.folders){
            final File file=new File((File)folder,pathname);
            if(file.exists()){
                return (IResourceStream)new FileResourceStream(file);
            }
        }
        if(!pathname.startsWith("WEB-INF/")){
            for(final String path : this.webappPaths){
                try{
                    final URL url=this.servletContext.getResource(path+pathname);
                    if(url!=null){
                        return (IResourceStream)new UrlResourceStream(url);
                    }
                    continue;
                }
                catch(Exception ex){
                }
            }
        }
        return null;
    }
    public String toString(){
        return "[folders = "+StringList.valueOf((Collection)this.folders)+", webapppaths: "+StringList.valueOf((Collection)this.webappPaths)+"]";
    }
    static{
        log=LoggerFactory.getLogger(WebApplicationPath.class);
    }
}
