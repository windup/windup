package org.apache.wicket.protocol.http;

import org.apache.wicket.util.lang.*;
import javax.servlet.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.string.*;
import java.io.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.cycle.*;
import javax.servlet.*;
import org.apache.wicket.*;
import org.apache.wicket.util.file.*;
import java.util.*;
import org.slf4j.*;

public class WicketFilter implements Filter{
    private static final Logger log;
    public static final String FILTER_MAPPING_PARAM="filterMappingUrlPattern";
    public static final String APP_FACT_PARAM="applicationFactoryClassName";
    public static final String IGNORE_PATHS_PARAM="ignorePaths";
    private WebApplication application;
    private IWebApplicationFactory applicationFactory;
    private FilterConfig filterConfig;
    private String filterPath;
    private int filterPathLength;
    private final Set<String> ignorePaths;
    private boolean isServlet;
    public WicketFilter(){
        super();
        this.filterPathLength=-1;
        this.ignorePaths=(Set<String>)new HashSet();
        this.isServlet=false;
    }
    public WicketFilter(final WebApplication application){
        super();
        this.filterPathLength=-1;
        this.ignorePaths=(Set<String>)new HashSet();
        this.isServlet=false;
        this.application=(WebApplication)Args.notNull((Object)application,"application");
    }
    protected ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }
    boolean processRequest(final ServletRequest request,final ServletResponse response,final FilterChain chain) throws IOException,ServletException{
        final ThreadContext previousThreadContext=ThreadContext.detach();
        boolean res=true;
        final ClassLoader previousClassLoader=Thread.currentThread().getContextClassLoader();
        final ClassLoader newClassLoader=this.getClassLoader();
        try{
            if(previousClassLoader!=newClassLoader){
                Thread.currentThread().setContextClassLoader(newClassLoader);
            }
            final HttpServletRequest httpServletRequest=(HttpServletRequest)request;
            final HttpServletResponse httpServletResponse=(HttpServletResponse)response;
            final String filterPath=this.getFilterPath(httpServletRequest);
            if(filterPath==null){
                throw new IllegalStateException("filter path was not configured");
            }
            if(this.shouldIgnorePath(httpServletRequest)){
                WicketFilter.log.debug("Ignoring request {}",httpServletRequest.getRequestURL());
                if(chain!=null){
                    chain.doFilter(request,response);
                }
                return false;
            }
            String redirectURL=this.checkIfRedirectRequired(httpServletRequest);
            if(redirectURL==null){
                ThreadContext.setApplication(this.application);
                final WebRequest webRequest=this.application.createWebRequest(httpServletRequest,filterPath);
                final WebResponse webResponse=this.application.createWebResponse(webRequest,httpServletResponse);
                final RequestCycle requestCycle=this.application.createRequestCycle((Request)webRequest,(Response)webResponse);
                if(!requestCycle.processRequestAndDetach()){
                    if(chain!=null){
                        chain.doFilter(request,response);
                    }
                    res=false;
                }
                else{
                    webResponse.flush();
                }
            }
            else{
                if(!Strings.isEmpty((CharSequence)httpServletRequest.getQueryString())){
                    redirectURL=redirectURL+"?"+httpServletRequest.getQueryString();
                }
                try{
                    httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL(redirectURL));
                }
                catch(IOException e){
                    throw new RuntimeException((Throwable)e);
                }
            }
        }
        finally{
            ThreadContext.restore(previousThreadContext);
            if(newClassLoader!=previousClassLoader){
                Thread.currentThread().setContextClassLoader(previousClassLoader);
            }
            if(response.isCommitted()){
                response.flushBuffer();
            }
        }
        return res;
    }
    public void doFilter(final ServletRequest request,final ServletResponse response,final FilterChain chain) throws IOException,ServletException{
        this.processRequest(request,response,chain);
    }
    protected IWebApplicationFactory getApplicationFactory(){
        final String appFactoryClassName=this.filterConfig.getInitParameter("applicationFactoryClassName");
        if(appFactoryClassName==null){
            return new ContextParamWebApplicationFactory();
        }
        try{
            final Class<?> factoryClass=(Class<?>)Class.forName(appFactoryClassName,false,Thread.currentThread().getContextClassLoader());
            return (IWebApplicationFactory)factoryClass.newInstance();
        }
        catch(ClassCastException e5){
            throw new WicketRuntimeException("Application factory class "+appFactoryClassName+" must implement IWebApplicationFactory");
        }
        catch(ClassNotFoundException e){
            throw new WebApplicationFactoryCreationException(appFactoryClassName,e);
        }
        catch(InstantiationException e2){
            throw new WebApplicationFactoryCreationException(appFactoryClassName,e2);
        }
        catch(IllegalAccessException e3){
            throw new WebApplicationFactoryCreationException(appFactoryClassName,e3);
        }
        catch(SecurityException e4){
            throw new WebApplicationFactoryCreationException(appFactoryClassName,e4);
        }
    }
    public final void init(final FilterConfig filterConfig) throws ServletException{
        this.init(false,filterConfig);
    }
    public void init(final boolean isServlet,final FilterConfig filterConfig) throws ServletException{
        this.filterConfig=filterConfig;
        this.isServlet=isServlet;
        this.initIgnorePaths(filterConfig);
        final ClassLoader previousClassLoader=Thread.currentThread().getContextClassLoader();
        final ClassLoader newClassLoader=this.getClassLoader();
        try{
            if(previousClassLoader!=newClassLoader){
                Thread.currentThread().setContextClassLoader(newClassLoader);
            }
            if(this.application==null){
                this.applicationFactory=this.getApplicationFactory();
                this.application=this.applicationFactory.createApplication(this);
            }
            this.application.setName(filterConfig.getFilterName());
            this.application.setWicketFilter(this);
            if(this.filterPath==null){
                this.filterPath=this.getFilterPathFromConfig(filterConfig);
            }
            if(this.filterPath==null){
                this.filterPath=this.getFilterPathFromWebXml(isServlet,filterConfig);
            }
            if(this.filterPath==null){
                this.filterPath=this.getFilterPathFromAnnotation(isServlet);
            }
            if(this.filterPath==null){
                WicketFilter.log.warn("Unable to determine filter path from filter init-parm, web.xml, or servlet 3.0 annotations. Assuming user will set filter path manually by calling setFilterPath(String)");
            }
            ThreadContext.setApplication(this.application);
            try{
                this.application.initApplication();
                this.application.logStarted();
            }
            finally{
                ThreadContext.detach();
            }
        }
        finally{
            if(newClassLoader!=previousClassLoader){
                Thread.currentThread().setContextClassLoader(previousClassLoader);
            }
        }
    }
    protected String getFilterPathFromAnnotation(final boolean isServlet){
        return null;
    }
    protected String getFilterPathFromWebXml(final boolean isServlet,final FilterConfig filterConfig){
        return new WebXmlFile().getUniqueFilterPath(isServlet,filterConfig);
    }
    public FilterConfig getFilterConfig(){
        return this.filterConfig;
    }
    protected String getFilterPath(final HttpServletRequest request){
        return this.filterPath;
    }
    protected String getFilterPathFromConfig(final FilterConfig filterConfig){
        final String result=filterConfig.getInitParameter("filterMappingUrlPattern");
        if(result!=null){
            if(result.equals("/*")){
                this.filterPath="";
            }
            else{
                if(!result.startsWith("/")||!result.endsWith("/*")){
                    throw new WicketRuntimeException("Your filterMappingUrlPattern must start with \"/\" and end with \"/*\". It is: "+result);
                }
                this.filterPath=result.substring(1,result.length()-1);
            }
        }
        return this.filterPath;
    }
    public void destroy(){
        if(this.application!=null){
            try{
                ThreadContext.setApplication(this.application);
                this.application.internalDestroy();
            }
            finally{
                ThreadContext.detach();
                this.application=null;
            }
        }
        if(this.applicationFactory!=null){
            this.applicationFactory.destroy(this);
        }
    }
    private String checkIfRedirectRequired(final HttpServletRequest request){
        return this.checkIfRedirectRequired(request.getRequestURI(),request.getContextPath());
    }
    protected final String checkIfRedirectRequired(final String requestURI,final String contextPath){
        int uriLength=requestURI.indexOf(59);
        if(uriLength==-1){
            uriLength=requestURI.length();
        }
        if(this.filterPathLength==-1){
            if(this.filterPath.endsWith("/")){
                this.filterPathLength=this.filterPath.length()-1;
            }
            else{
                this.filterPathLength=this.filterPath.length();
            }
        }
        final int homePathLength=contextPath.length()+((this.filterPathLength>0)?(1+this.filterPathLength):0);
        if(uriLength!=homePathLength){
            return null;
        }
        String uri=Strings.stripJSessionId(requestURI);
        String homePageUri=contextPath+"/"+this.filterPath;
        if(homePageUri.endsWith("/")){
            homePageUri=homePageUri.substring(0,homePageUri.length()-1);
        }
        if(uri.equals(homePageUri)){
            uri+="/";
            return uri;
        }
        return null;
    }
    public final void setFilterPath(final String filterPath){
        if(this.filterPath!=null){
            throw new IllegalStateException("Filter path is write-once. You can not change it. Current value='"+filterPath+"'");
        }
        this.filterPath=filterPath;
    }
    public String getRelativePath(final HttpServletRequest request){
        String path=Strings.stripJSessionId(request.getRequestURI());
        final String contextPath=request.getContextPath();
        path=path.substring(contextPath.length());
        if(this.isServlet){
            final String servletPath=request.getServletPath();
            path=path.substring(servletPath.length());
        }
        if(path.length()>0){
            path=path.substring(1);
        }
        if(!path.startsWith(this.filterPath)&&this.filterPath.equals(path+"/")){
            path+="/";
        }
        if(path.startsWith(this.filterPath)){
            path=path.substring(this.filterPath.length());
        }
        return path;
    }
    private boolean shouldIgnorePath(final HttpServletRequest request){
        boolean ignore=false;
        if(this.ignorePaths.size()>0){
            final String relativePath=this.getRelativePath(request);
            if(!Strings.isEmpty((CharSequence)relativePath)){
                for(final String path : this.ignorePaths){
                    if(relativePath.startsWith(path)){
                        ignore=true;
                        break;
                    }
                }
            }
        }
        return ignore;
    }
    private void initIgnorePaths(final FilterConfig filterConfig){
        final String paths=filterConfig.getInitParameter("ignorePaths");
        if(!Strings.isEmpty((CharSequence)paths)){
            final String[] arr$;
            final String[] parts=arr$=Strings.split(paths,',');
            for(String path : arr$){
                path=path.trim();
                if(path.startsWith("/")){
                    path=path.substring(1);
                }
                this.ignorePaths.add(path);
            }
        }
    }
    static{
        log=LoggerFactory.getLogger(WicketFilter.class);
    }
}
