package org.apache.wicket.protocol.http.mock;

import org.apache.wicket.util.value.*;
import javax.servlet.http.*;
import org.apache.wicket.mock.*;
import org.apache.wicket.util.file.*;
import java.nio.charset.*;
import java.text.*;
import javax.servlet.*;
import java.security.*;
import java.util.*;
import org.apache.wicket.request.*;
import java.io.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class MockHttpServletRequest implements HttpServletRequest{
    private static final Logger log;
    private final Application application;
    private final ValueMap attributes;
    private String authType;
    private String characterEncoding;
    private final ServletContext context;
    private final List<Cookie> cookies;
    private final ValueMap headers;
    private String method;
    private final LinkedHashMap<String,String[]> parameters;
    private String path;
    private final HttpSession session;
    private String url;
    private Map<String,UploadedFile> uploadedFiles;
    private boolean useMultiPartContentType;
    private boolean secure;
    private String remoteAddr;
    private String scheme;
    private String serverName;
    private int serverPort;
    private static final String crlf="\r\n";
    private static final String boundary="--abcdefgABCDEFG";
    private final MockRequestParameters post;
    public MockHttpServletRequest(final Application application,final HttpSession session,final ServletContext context){
        super();
        this.attributes=new ValueMap();
        this.characterEncoding="UTF-8";
        this.cookies=(List<Cookie>)new ArrayList();
        this.headers=new ValueMap();
        this.parameters=new LinkedHashMap<String,String[]>();
        this.secure=false;
        this.remoteAddr="127.0.0.1";
        this.scheme="http";
        this.serverName="localhost";
        this.serverPort=80;
        this.post=new MockRequestParameters();
        this.application=application;
        this.session=session;
        this.context=context;
        this.initialize();
    }
    public void addCookie(final Cookie cookie){
        this.cookies.add(cookie);
    }
    public void addCookies(final Iterable<Cookie> cookies){
        for(final Cookie cookie : cookies){
            this.addCookie(cookie);
        }
    }
    public void addFile(final String fieldName,final File file,final String contentType){
        if(file==null){
            throw new IllegalArgumentException("File must not be null");
        }
        if(!file.exists()){
            throw new IllegalArgumentException("File does not exists. You must provide an existing file: "+file.getAbsolutePath());
        }
        if(!file.isFile()){
            throw new IllegalArgumentException("You can only add a File, which is not a directory. Only files can be uploaded.");
        }
        if(this.uploadedFiles==null){
            this.uploadedFiles=(Map<String,UploadedFile>)new HashMap();
        }
        final UploadedFile uf=new UploadedFile(fieldName,file,contentType);
        this.uploadedFiles.put(fieldName,uf);
        this.setUseMultiPartContentType(true);
    }
    public void addHeader(final String name,final String value){
        List<String> list=(List<String>)this.headers.get((Object)name);
        if(list==null){
            list=(List<String>)new ArrayList(1);
            this.headers.put(name,(Object)list);
        }
        list.add(value);
    }
    public void setHeader(final String name,final String value){
        List<String> list=(List<String>)this.headers.get((Object)name);
        if(list==null){
            list=(List<String>)new ArrayList(1);
            this.headers.put(name,(Object)list);
        }
        list.clear();
        list.add(value);
    }
    public void addDateHeader(final String name,final long date){
        final DateFormat df=DateFormat.getDateInstance(0);
        final String dateString=df.format(new Date(date));
        this.addHeader(name,dateString);
    }
    public Object getAttribute(final String name){
        return this.attributes.get((Object)name);
    }
    public Enumeration<String> getAttributeNames(){
        return (Enumeration<String>)Collections.enumeration(this.attributes.keySet());
    }
    public String getAuthType(){
        return this.authType;
    }
    public String getCharacterEncoding(){
        return this.characterEncoding;
    }
    public Charset getCharset(){
        return Charset.forName(this.characterEncoding);
    }
    public void setUseMultiPartContentType(final boolean useMultiPartContentType){
        this.useMultiPartContentType=useMultiPartContentType;
    }
    public int getContentLength(){
        if(this.useMultiPartContentType){
            final byte[] request=this.buildRequest();
            return request.length;
        }
        return -1;
    }
    public String getContentType(){
        if(this.useMultiPartContentType){
            return "multipart/form-data; boundary=abcdefgABCDEFG";
        }
        return null;
    }
    public String getContextPath(){
        return "/context";
    }
    public Cookie getCookie(final String name){
        final Cookie[] cookies=this.getCookies();
        if(cookies==null){
            return null;
        }
        for(final Cookie cookie : cookies){
            if(cookie.getName().equals(name)){
                return Cookies.copyOf(cookie);
            }
        }
        return null;
    }
    public Cookie[] getCookies(){
        if(this.cookies.size()==0){
            return null;
        }
        final Cookie[] result=new Cookie[this.cookies.size()];
        for(int i=0;i<this.cookies.size();++i){
            result[i]=Cookies.copyOf((Cookie)this.cookies.get(i));
        }
        return result;
    }
    public long getDateHeader(final String name) throws IllegalArgumentException{
        final String value=this.getHeader(name);
        if(value==null){
            return -1L;
        }
        final DateFormat df=DateFormat.getDateInstance(0);
        try{
            return df.parse(value).getTime();
        }
        catch(ParseException e){
            throw new IllegalArgumentException("Can't convert header to date "+name+": "+value);
        }
    }
    public String getHeader(final String name){
        final List<String> l=(List<String>)this.headers.get((Object)name);
        if(l==null||l.size()<1){
            return null;
        }
        return (String)l.get(0);
    }
    public Enumeration<String> getHeaderNames(){
        return (Enumeration<String>)Collections.enumeration(this.headers.keySet());
    }
    public Enumeration<String> getHeaders(final String name){
        List<String> list=(List<String>)this.headers.get((Object)name);
        if(list==null){
            list=(List<String>)new ArrayList();
        }
        return (Enumeration<String>)Collections.enumeration(list);
    }
    public ServletInputStream getInputStream() throws IOException{
        final byte[] request=this.buildRequest();
        final ByteArrayInputStream bais=new ByteArrayInputStream(request);
        return new ServletInputStream(){
            public int read(){
                return bais.read();
            }
        };
    }
    public int getIntHeader(final String name){
        final String value=this.getHeader(name);
        if(value==null){
            return -1;
        }
        return Integer.valueOf(value);
    }
    public Locale getLocale(){
        return (Locale)this.getLocales().nextElement();
    }
    private Locale getLocale(final String value){
        final String[] bits=value.split("-");
        if(bits.length<1){
            return null;
        }
        final String language=bits[0].toLowerCase();
        if(bits.length>1){
            final String country=bits[1].toUpperCase();
            return new Locale(language,country);
        }
        return new Locale(language);
    }
    public Enumeration<Locale> getLocales(){
        final List<Locale> list=(List<Locale>)new ArrayList();
        final String header=this.getHeader("Accept-Language");
        if(header!=null){
            final String[] arr$;
            final String[] locales=arr$=header.split(",");
            for(final String value : arr$){
                final Locale locale=this.getLocale(value);
                if(locale!=null){
                    list.add(locale);
                }
            }
        }
        if(list.size()==0){
            list.add(Locale.getDefault());
        }
        return (Enumeration<Locale>)Collections.enumeration(list);
    }
    public String getMethod(){
        return this.method;
    }
    public String getParameter(final String name){
        final String[] param=(String[])this.getParameterMap().get(name);
        if(param==null){
            return null;
        }
        return param[0];
    }
    public Map<String,String[]> getParameterMap(){
        final Map<String,String[]> params=(Map<String,String[]>)new HashMap(this.parameters);
        for(final String name : this.post.getParameterNames()){
            final List<StringValue> values=this.post.getParameterValues(name);
            for(final StringValue value : values){
                final String[] present=(String[])params.get(name);
                if(present==null){
                    params.put(name,new String[] { value.toString() });
                }
                else{
                    final String[] newval=new String[present.length+1];
                    System.arraycopy(present,0,newval,0,present.length);
                    newval[newval.length-1]=value.toString();
                    params.put(name,newval);
                }
            }
        }
        return params;
    }
    public Enumeration<String> getParameterNames(){
        return (Enumeration<String>)Collections.enumeration(this.getParameterMap().keySet());
    }
    public String[] getParameterValues(final String name){
        final Object value=this.getParameterMap().get(name);
        if(value==null){
            return new String[0];
        }
        if(value instanceof String[]){
            return (String[])value;
        }
        final String[] result= { value.toString() };
        return result;
    }
    public String getPathInfo(){
        return this.path;
    }
    public String getPathTranslated(){
        return null;
    }
    public String getProtocol(){
        return "HTTP/1.1";
    }
    public String getQueryString(){
        if(this.parameters.size()==0){
            return null;
        }
        final StringBuilder buf=new StringBuilder();
        final Iterator<String> iterator=(Iterator<String>)this.parameters.keySet().iterator();
        while(iterator.hasNext()){
            final String name=(String)iterator.next();
            final String[] values=this.getParameterValues(name);
            for(int i=0;i<values.length;++i){
                if(name!=null){
                    buf.append(UrlEncoder.QUERY_INSTANCE.encode(name,this.getCharset()));
                }
                buf.append('=');
                if(values[i]!=null){
                    buf.append(UrlEncoder.QUERY_INSTANCE.encode(values[i],this.getCharset()));
                }
                if(i+1<values.length){
                    buf.append('&');
                }
            }
            if(iterator.hasNext()){
                buf.append('&');
            }
        }
        return buf.toString();
    }
    public BufferedReader getReader() throws IOException{
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
    @Deprecated
    public String getRealPath(final String name){
        return this.context.getRealPath(name);
    }
    public String getRemoteAddr(){
        return this.remoteAddr;
    }
    public void setRemoteAddr(final String addr){
        this.remoteAddr=addr;
    }
    public String getRemoteHost(){
        return "localhost";
    }
    public String getRemoteUser(){
        return this.getHeader("REMOTE_USER");
    }
    public RequestDispatcher getRequestDispatcher(final String name){
        return this.context.getRequestDispatcher(name);
    }
    public String getRequestedSessionId(){
        if(this.session instanceof MockHttpSession&&((MockHttpSession)this.session).isTemporary()){
            return null;
        }
        return this.session.getId();
    }
    public String getRequestURI(){
        if(this.url==null){
            return this.getContextPath()+this.getServletPath();
        }
        final int index=this.url.indexOf("?");
        if(index!=-1){
            return this.url.substring(0,index);
        }
        return this.url;
    }
    public StringBuffer getRequestURL(){
        final StringBuffer buf=new StringBuffer();
        buf.append("http://localhost");
        buf.append(this.getContextPath());
        if(this.getPathInfo()!=null){
            buf.append(this.getPathInfo());
        }
        return buf;
    }
    public String getScheme(){
        return this.scheme;
    }
    public void setScheme(final String scheme){
        this.scheme=scheme;
        this.secure="https".equalsIgnoreCase(scheme);
    }
    public String getServerName(){
        return this.serverName;
    }
    public void setServerName(final String serverName){
        this.serverName=serverName;
    }
    public int getServerPort(){
        return this.serverPort;
    }
    public void setServerPort(final int port){
        this.serverPort=port;
    }
    public String getServletPath(){
        return "/servlet";
    }
    public HttpSession getSession(){
        return this.getSession(true);
    }
    public HttpSession getSession(final boolean b){
        HttpSession sess=null;
        if(this.session instanceof MockHttpSession){
            final MockHttpSession mockHttpSession=(MockHttpSession)this.session;
            if(b){
                mockHttpSession.setTemporary(false);
            }
            if(!mockHttpSession.isTemporary()){
                sess=this.session;
            }
        }
        return sess;
    }
    public Principal getUserPrincipal(){
        final String user=this.getRemoteUser();
        if(user==null){
            return null;
        }
        return new Principal(){
            public String getName(){
                return user;
            }
        };
    }
    public boolean hasUploadedFiles(){
        return this.uploadedFiles!=null;
    }
    public void initialize(){
        this.authType=null;
        this.method="post";
        this.cookies.clear();
        this.setDefaultHeaders();
        this.path=null;
        this.url=null;
        this.characterEncoding="UTF-8";
        this.parameters.clear();
        this.attributes.clear();
        this.post.reset();
    }
    public boolean isRequestedSessionIdFromCookie(){
        return true;
    }
    public boolean isRequestedSessionIdFromUrl(){
        return false;
    }
    public boolean isRequestedSessionIdFromURL(){
        return false;
    }
    public boolean isRequestedSessionIdValid(){
        return true;
    }
    public boolean isSecure(){
        return this.secure;
    }
    public void setSecure(final boolean secure){
        this.secure=secure;
    }
    public boolean isUserInRole(final String name){
        return false;
    }
    public void removeAttribute(final String name){
        this.attributes.remove((Object)name);
    }
    public void setAttribute(final String name,final Object o){
        this.attributes.put(name,o);
    }
    public void setAuthType(final String authType){
        this.authType=authType;
    }
    public void setCharacterEncoding(final String encoding) throws UnsupportedEncodingException{
        this.characterEncoding=encoding;
    }
    public void setCookies(final Cookie[] theCookies){
        this.cookies.clear();
        this.addCookies((Iterable<Cookie>)Arrays.asList(theCookies));
    }
    public void setMethod(final String method){
        this.method=method;
    }
    public void setParameter(final String name,final String value){
        if(value==null){
            this.parameters.remove(name);
        }
        else{
            this.parameters.put(name,new String[] { value });
        }
    }
    public void addParameter(final String name,final String value){
        if(value==null){
            return;
        }
        final String[] val=this.parameters.get(name);
        if(val==null){
            this.parameters.put(name,new String[] { value });
        }
        else{
            final String[] newval=new String[val.length+1];
            System.arraycopy(val,0,newval,0,val.length);
            newval[val.length]=value;
            this.parameters.put(name,newval);
        }
    }
    public void setParameters(final Map<String,String[]> parameters){
        this.parameters.putAll(parameters);
    }
    public void setPath(final String path){
        this.path=UrlDecoder.PATH_INSTANCE.decode(path,this.getCharset());
    }
    public void setURL(final String url){
        this.setUrl(Url.parse(url));
    }
    private void setDefaultHeaders(){
        this.headers.clear();
        this.addHeader("Accept","text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        this.addHeader("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        final Locale l=Locale.getDefault();
        this.addHeader("Accept-Language",l.getLanguage().toLowerCase()+"-"+l.getCountry().toLowerCase()+","+l.getLanguage().toLowerCase()+";q=0.5");
        this.addHeader("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.7) Gecko/20040707 Firefox/0.9.2");
    }
    private void newAttachment(final OutputStream out) throws IOException{
        out.write("--abcdefgABCDEFG".getBytes());
        out.write("\r\n".getBytes());
        out.write("Content-Disposition: form-data".getBytes());
    }
    private byte[] buildRequest(){
        if(this.uploadedFiles==null&&!this.useMultiPartContentType){
            if(this.post.getParameterNames().size()==0){
                return "".getBytes();
            }
            final Url url=new Url();
            for(final String parameterName : this.post.getParameterNames()){
                final List<StringValue> values=this.post.getParameterValues(parameterName);
                for(final StringValue value : values){
                    url.addQueryParameter(parameterName,(Object)value.toString());
                }
            }
            final String body=url.toString().substring(1);
            return body.getBytes();
        }
        else{
            try{
                final ByteArrayOutputStream out=new ByteArrayOutputStream();
                for(final String parameterName : this.post.getParameterNames()){
                    final List<StringValue> values=this.post.getParameterValues(parameterName);
                    for(final StringValue value : values){
                        this.newAttachment(out);
                        out.write("; name=\"".getBytes());
                        out.write(parameterName.getBytes());
                        out.write("\"".getBytes());
                        out.write("\r\n".getBytes());
                        out.write("\r\n".getBytes());
                        out.write(this.post.getParameterValue(parameterName).toString().getBytes());
                        out.write("\r\n".getBytes());
                    }
                }
                if(this.uploadedFiles!=null){
                    for(final String fieldName : this.uploadedFiles.keySet()){
                        final UploadedFile uf=(UploadedFile)this.uploadedFiles.get(fieldName);
                        this.newAttachment(out);
                        out.write("; name=\"".getBytes());
                        out.write(fieldName.getBytes());
                        out.write("\"; filename=\"".getBytes());
                        out.write(uf.getFile().getName().getBytes());
                        out.write("\"".getBytes());
                        out.write("\r\n".getBytes());
                        out.write("Content-Type: ".getBytes());
                        out.write(uf.getContentType().getBytes());
                        out.write("\r\n".getBytes());
                        out.write("\r\n".getBytes());
                        final FileInputStream fis=new FileInputStream((java.io.File)uf.getFile());
                        try{
                            IOUtils.copy((InputStream)fis,(OutputStream)out);
                        }
                        finally{
                            fis.close();
                        }
                        out.write("\r\n".getBytes());
                    }
                }
                out.write("--abcdefgABCDEFG".getBytes());
                out.write("--".getBytes());
                out.write("\r\n".getBytes());
                return out.toByteArray();
            }
            catch(IOException e){
                throw new WicketRuntimeException(e);
            }
        }
    }
    public String getLocalAddr(){
        return "127.0.0.1";
    }
    public String getLocalName(){
        return "127.0.0.1";
    }
    public int getLocalPort(){
        return 80;
    }
    public int getRemotePort(){
        return 80;
    }
    public void setUrl(final Url url){
        if(url.getProtocol()!=null){
            this.setScheme(url.getProtocol());
        }
        if(url.getHost()!=null){
            this.serverName=url.getHost();
        }
        if(url.getPort()!=null){
            this.serverPort=url.getPort();
        }
        String path=url.getPath(this.getCharset());
        if(!path.startsWith("/")){
            path=this.getContextPath()+this.getServletPath()+'/'+path;
        }
        this.url=path;
        if(path.startsWith(this.getContextPath())){
            path=path.substring(this.getContextPath().length());
        }
        if(path.startsWith(this.getServletPath())){
            path=path.substring(this.getServletPath().length());
        }
        this.setPath(path);
        for(final Url.QueryParameter parameter : url.getQueryParameters()){
            this.addParameter(parameter.getName(),parameter.getValue());
        }
    }
    public Url getUrl(){
        final String queryString=this.getQueryString();
        String urlString;
        if(Strings.isEmpty((CharSequence)queryString)){
            urlString=this.getRequestURI();
        }
        else{
            urlString=this.getRequestURI()+'?'+queryString;
        }
        final Url url=Url.parse(urlString,this.getCharset());
        url.setProtocol(this.scheme);
        url.setHost(this.serverName);
        url.setPort(this.serverPort);
        return url;
    }
    public MockRequestParameters getPostParameters(){
        return this.post;
    }
    public String getFilterPrefix(){
        return this.getServletPath().substring(1);
    }
    public ServletContext getServletContext(){
        return this.context;
    }
    static{
        log=LoggerFactory.getLogger(MockHttpServletRequest.class);
    }
    private static class UploadedFile{
        private String fieldName;
        private File file;
        private String contentType;
        public UploadedFile(final String fieldName,final File file,final String contentType){
            super();
            this.fieldName=fieldName;
            this.file=file;
            this.contentType=contentType;
        }
        public String getContentType(){
            return this.contentType;
        }
        public void setContentType(final String contentType){
            this.contentType=contentType;
        }
        public String getFieldName(){
            return this.fieldName;
        }
        public void setFieldName(final String fieldName){
            this.fieldName=fieldName;
        }
        public File getFile(){
            return this.file;
        }
        public void setFile(final File file){
            this.file=file;
        }
    }
}
