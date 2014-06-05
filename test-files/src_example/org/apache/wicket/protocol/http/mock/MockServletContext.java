package org.apache.wicket.protocol.http.mock;

import org.apache.wicket.*;
import org.apache.wicket.util.value.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import org.slf4j.*;

public class MockServletContext implements ServletContext{
    private static final Logger log;
    private final Application application;
    private final ValueMap attributes;
    private final ValueMap initParameters;
    private final ValueMap mimeTypes;
    private File webappRoot;
    public MockServletContext(final Application application,final String path){
        super();
        this.attributes=new ValueMap();
        this.initParameters=new ValueMap();
        this.mimeTypes=new ValueMap();
        this.application=application;
        this.webappRoot=null;
        if(path!=null){
            this.webappRoot=new File(path);
            if(!this.webappRoot.exists()||!this.webappRoot.isDirectory()){
                MockServletContext.log.warn("WARNING: The webapp root directory is invalid: "+path);
                this.webappRoot=null;
            }
        }
        final String workFolder=System.getProperty("wicket.tester.work.folder","target/work/");
        File file=new File(workFolder);
        try{
            file.mkdirs();
        }
        catch(SecurityException sx){
            final String tmpDir=System.getProperty("java.io.tmpdir");
            file=new File(tmpDir);
        }
        this.attributes.put("javax.servlet.context.tempdir",(Object)file);
        this.mimeTypes.put("html",(Object)"text/html");
        this.mimeTypes.put("htm",(Object)"text/html");
        this.mimeTypes.put("css",(Object)"text/css");
        this.mimeTypes.put("xml",(Object)"text/xml");
        this.mimeTypes.put("js",(Object)"text/javascript");
        this.mimeTypes.put("gif",(Object)"image/gif");
        this.mimeTypes.put("jpg",(Object)"image/jpeg");
        this.mimeTypes.put("png",(Object)"image/png");
    }
    public void addInitParameter(final String name,final String value){
        this.initParameters.put(name,(Object)value);
    }
    public void addMimeType(final String fileExtension,final String mimeType){
        this.mimeTypes.put(fileExtension,(Object)mimeType);
    }
    public Object getAttribute(final String name){
        return this.attributes.get((Object)name);
    }
    public Enumeration<String> getAttributeNames(){
        return (Enumeration<String>)Collections.enumeration(this.attributes.keySet());
    }
    public ServletContext getContext(final String name){
        return this;
    }
    public String getInitParameter(final String name){
        return this.initParameters.getString(name);
    }
    public Enumeration<String> getInitParameterNames(){
        return (Enumeration<String>)Collections.enumeration(this.initParameters.keySet());
    }
    public int getMajorVersion(){
        return 2;
    }
    public String getMimeType(final String name){
        final int index=name.lastIndexOf(46);
        if(index==-1||index==name.length()-1){
            return null;
        }
        return this.mimeTypes.getString(name.substring(index+1));
    }
    public int getMinorVersion(){
        return 5;
    }
    public RequestDispatcher getNamedDispatcher(final String name){
        return this.getRequestDispatcher(name);
    }
    public String getRealPath(String name){
        if(this.webappRoot==null){
            return null;
        }
        if(name.startsWith("/")){
            name=name.substring(1);
        }
        final File f=new File(this.webappRoot,name);
        if(!f.exists()){
            return null;
        }
        return f.getPath();
    }
    public RequestDispatcher getRequestDispatcher(final String name){
        return new RequestDispatcher(){
            public void forward(final ServletRequest servletRequest,final ServletResponse servletResponse) throws IOException{
                servletResponse.getWriter().write("FORWARD TO RESOURCE: "+name);
            }
            public void include(final ServletRequest servletRequest,final ServletResponse servletResponse) throws IOException{
                servletResponse.getWriter().write("INCLUDE OF RESOURCE: "+name);
            }
        };
    }
    public URL getResource(String name) throws MalformedURLException{
        if(this.webappRoot==null){
            return null;
        }
        if(name.startsWith("/")){
            name=name.substring(1);
        }
        final File f=new File(this.webappRoot,name);
        if(!f.exists()){
            return null;
        }
        return f.toURI().toURL();
    }
    public InputStream getResourceAsStream(String name){
        if(this.webappRoot==null){
            return null;
        }
        if(name.startsWith("/")){
            name=name.substring(1);
        }
        final File f=new File(this.webappRoot,name);
        if(!f.exists()){
            return null;
        }
        try{
            return new FileInputStream(f);
        }
        catch(FileNotFoundException e){
            MockServletContext.log.error(e.getMessage(),e);
            return null;
        }
    }
    public Set<String> getResourcePaths(String name){
        if(this.webappRoot==null){
            return (Set<String>)new HashSet();
        }
        if(name.startsWith("/")){
            name=name.substring(1);
        }
        if(name.endsWith("/")){
            name=name.substring(0,name.length()-1);
        }
        String[] elements=null;
        if(name.trim().length()==0){
            elements=new String[0];
        }
        else{
            elements=name.split("/");
        }
        File current=this.webappRoot;
        for(final String element : elements){
            final File[] files=current.listFiles();
            boolean match=false;
            for(final File file : files){
                if(file.getName().equals(element)&&file.isDirectory()){
                    current=file;
                    match=true;
                    break;
                }
            }
            if(!match){
                return null;
            }
        }
        final File[] files2=current.listFiles();
        final Set<String> result=(Set<String>)new HashSet();
        final int stripLength=this.webappRoot.getPath().length();
        for(final File file2 : files2){
            String s=file2.getPath().substring(stripLength).replace('\\','/');
            if(file2.isDirectory()){
                s+="/";
            }
            result.add(s);
        }
        return result;
    }
    public String getServerInfo(){
        return "Wicket Mock Test Environment v1.0";
    }
    public Servlet getServlet(final String name) throws ServletException{
        return null;
    }
    public String getServletContextName(){
        return this.application.getName();
    }
    public Enumeration<String> getServletNames(){
        return null;
    }
    public Enumeration<Servlet> getServlets(){
        return null;
    }
    public void log(final Exception e,final String msg){
        MockServletContext.log.error(msg,e);
    }
    public void log(final String msg){
        MockServletContext.log.info(msg);
    }
    public void log(final String msg,final Throwable cause){
        MockServletContext.log.error(msg,cause);
    }
    public void removeAttribute(final String name){
        this.attributes.remove((Object)name);
    }
    public void setAttribute(final String name,final Object o){
        this.attributes.put(name,o);
    }
    public String getContextPath(){
        return "";
    }
    static{
        log=LoggerFactory.getLogger(MockServletContext.class);
    }
}
