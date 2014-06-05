package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.request.http.*;
import org.apache.wicket.request.http.flow.*;
import javax.servlet.http.*;
import org.apache.wicket.util.time.*;
import javax.servlet.*;
import java.util.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.upload.*;
import java.nio.charset.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class ServletWebRequest extends WebRequest{
    private static final Logger LOG;
    private final HttpServletRequest httpServletRequest;
    private final Url url;
    private final String filterPrefix;
    private final ErrorAttributes errorAttributes;
    private final ForwardAttributes forwardAttributes;
    private Map<String,List<StringValue>> postParameters;
    private final IRequestParameters postRequestParameters;
    private static final Logger logger;
    public ServletWebRequest(final HttpServletRequest httpServletRequest,final String filterPrefix){
        this(httpServletRequest,filterPrefix,null);
    }
    public ServletWebRequest(final HttpServletRequest httpServletRequest,final String filterPrefix,final Url url){
        super();
        this.postParameters=null;
        this.postRequestParameters=(IRequestParameters)new IWritableRequestParameters(){
            public void reset(){
                ServletWebRequest.this.getPostRequestParameters().clear();
            }
            public void setParameterValues(final String key,final List<StringValue> values){
                ServletWebRequest.this.getPostRequestParameters().put(key,values);
            }
            public Set<String> getParameterNames(){
                return (Set<String>)Collections.unmodifiableSet(ServletWebRequest.this.getPostRequestParameters().keySet());
            }
            public StringValue getParameterValue(final String name){
                final List<StringValue> values=(List<StringValue>)ServletWebRequest.this.getPostRequestParameters().get(name);
                if(values==null||values.isEmpty()){
                    return StringValue.valueOf((String)null);
                }
                return (StringValue)values.iterator().next();
            }
            public List<StringValue> getParameterValues(final String name){
                List<StringValue> values=(List<StringValue>)ServletWebRequest.this.getPostRequestParameters().get(name);
                if(values!=null){
                    values=(List<StringValue>)Collections.unmodifiableList(values);
                }
                return values;
            }
        };
        Args.notNull((Object)httpServletRequest,"httpServletRequest");
        Args.notNull((Object)filterPrefix,"filterPrefix");
        this.httpServletRequest=httpServletRequest;
        this.errorAttributes=ErrorAttributes.of(httpServletRequest,filterPrefix);
        this.forwardAttributes=ForwardAttributes.of(httpServletRequest,filterPrefix);
        if(this.forwardAttributes!=null||this.errorAttributes!=null){
            if(ServletWebRequest.LOG.isDebugEnabled()){
                ServletWebRequest.LOG.debug("Setting filterPrefix('{}') to '' because there is either an error or a forward. {}, {}",filterPrefix,this.forwardAttributes,this.errorAttributes);
            }
            this.filterPrefix="";
        }
        else{
            this.filterPrefix=filterPrefix;
        }
        if(url!=null){
            this.url=url;
        }
        else{
            this.url=this.getContextRelativeUrl(httpServletRequest.getRequestURI(),filterPrefix);
        }
    }
    public Url getClientUrl(){
        if(this.errorAttributes!=null&&!Strings.isEmpty((CharSequence)this.errorAttributes.getRequestUri())){
            final String problematicURI=Url.parse(this.errorAttributes.getRequestUri(),this.getCharset()).toString();
            return this.getContextRelativeUrl(problematicURI,this.filterPrefix);
        }
        if(this.forwardAttributes!=null&&!Strings.isEmpty((CharSequence)this.forwardAttributes.getRequestUri())){
            final String forwardURI=Url.parse(this.forwardAttributes.getRequestUri(),this.getCharset()).toString();
            return this.getContextRelativeUrl(forwardURI,this.filterPrefix);
        }
        if(!this.isAjax()){
            return this.getContextRelativeUrl(this.httpServletRequest.getRequestURI(),this.filterPrefix);
        }
        String base=this.getHeader("Wicket-Ajax-BaseURL");
        if(base==null){
            base=this.getRequestParameters().getParameterValue("wicket-ajax-baseurl").toString((String)null);
        }
        if(base==null){
            throw new AbortWithHttpErrorCodeException(400,"Current ajax request is missing the base url header or parameter");
        }
        return this.setParameters(Url.parse(base,this.getCharset()));
    }
    private Url setParameters(final Url url){
        url.setPort(this.httpServletRequest.getServerPort());
        url.setHost(this.httpServletRequest.getServerName());
        url.setProtocol(this.httpServletRequest.getScheme());
        return url;
    }
    private Url getContextRelativeUrl(String uri,String filterPrefix){
        if(filterPrefix.length()>0&&!filterPrefix.endsWith("/")){
            filterPrefix+="/";
        }
        final StringBuilder url=new StringBuilder();
        uri=Strings.stripJSessionId(uri);
        final String contextPath=this.httpServletRequest.getContextPath();
        if(ServletWebRequest.LOG.isDebugEnabled()){
            ServletWebRequest.LOG.debug("Calculating context relative path from: context path '{}', filterPrefix '{}', uri '{}'",contextPath,filterPrefix,uri);
        }
        final int start=contextPath.length()+filterPrefix.length()+1;
        if(uri.length()>start){
            url.append(uri.substring(start));
        }
        if(this.errorAttributes==null){
            final String query=this.httpServletRequest.getQueryString();
            if(!Strings.isEmpty((CharSequence)query)){
                url.append('?');
                url.append(query);
            }
        }
        return this.setParameters(Url.parse(url.toString(),this.getCharset()));
    }
    public String getFilterPrefix(){
        return this.filterPrefix;
    }
    public List<Cookie> getCookies(){
        final Cookie[] cookies=this.httpServletRequest.getCookies();
        final List<Cookie> result=(List<Cookie>)((cookies==null)?Collections.emptyList():Arrays.asList(cookies));
        return (List<Cookie>)Collections.unmodifiableList(result);
    }
    public Locale getLocale(){
        return this.httpServletRequest.getLocale();
    }
    public Time getDateHeader(final String name){
        try{
            final long value=this.httpServletRequest.getDateHeader(name);
            if(value==-1L){
                return null;
            }
            return Time.millis(value);
        }
        catch(IllegalArgumentException e){
            return null;
        }
    }
    public String getHeader(final String name){
        return this.httpServletRequest.getHeader(name);
    }
    public List<String> getHeaders(final String name){
        final List<String> result=(List<String>)new ArrayList();
        final Enumeration<String> e=(Enumeration<String>)this.httpServletRequest.getHeaders(name);
        while(e.hasMoreElements()){
            result.add(e.nextElement());
        }
        return (List<String>)Collections.unmodifiableList(result);
    }
    private static boolean isMultiPart(final ServletRequest request){
        final String contentType=request.getContentType();
        return contentType!=null&&contentType.toLowerCase().contains((CharSequence)"multipart");
    }
    protected Map<String,List<StringValue>> generatePostParameters(){
        final Map<String,List<StringValue>> postParameters=(Map<String,List<StringValue>>)new HashMap();
        final IRequestParameters queryParams=this.getQueryParameters();
        final Map<String,String[]> params=(Map<String,String[]>)this.getContainerRequest().getParameterMap();
        for(final Map.Entry<String,String[]> param : params.entrySet()){
            final String name=(String)param.getKey();
            final String[] values=(String[])param.getValue();
            if(name!=null&&values!=null){
                List<StringValue> queryValues=(List<StringValue>)queryParams.getParameterValues(name);
                if(queryValues==null){
                    queryValues=(List<StringValue>)Collections.emptyList();
                }
                else{
                    queryValues=(List<StringValue>)new ArrayList(queryValues);
                }
                final List<StringValue> postValues=(List<StringValue>)new ArrayList();
                for(final String value : values){
                    final StringValue val=StringValue.valueOf(value);
                    if(queryValues.contains(val)){
                        queryValues.remove(val);
                    }
                    else{
                        postValues.add(val);
                    }
                }
                if(postValues.isEmpty()){
                    continue;
                }
                postParameters.put(name,postValues);
            }
        }
        return postParameters;
    }
    private Map<String,List<StringValue>> getPostRequestParameters(){
        if(this.postParameters==null){
            this.postParameters=this.generatePostParameters();
        }
        return this.postParameters;
    }
    public IRequestParameters getPostParameters(){
        return this.postRequestParameters;
    }
    public Url getUrl(){
        return new Url(this.url);
    }
    public ServletWebRequest cloneWithUrl(final Url url){
        return new ServletWebRequest(this.httpServletRequest,this.filterPrefix,url){
            public Url getOriginalUrl(){
                return ServletWebRequest.this.getOriginalUrl();
            }
            public IRequestParameters getPostParameters(){
                return ServletWebRequest.this.getPostParameters();
            }
        };
    }
    public MultipartServletWebRequest newMultipartWebRequest(final Bytes maxSize,final String upload) throws FileUploadException{
        return new MultipartServletWebRequestImpl(this.getContainerRequest(),this.filterPrefix,maxSize,upload);
    }
    public MultipartServletWebRequest newMultipartWebRequest(final Bytes maxSize,final String upload,final FileItemFactory factory) throws FileUploadException{
        return new MultipartServletWebRequestImpl(this.getContainerRequest(),this.filterPrefix,maxSize,upload,factory);
    }
    public String getPrefixToContextPath(){
        final PrependingStringBuffer buffer=new PrependingStringBuffer();
        final Url filterPrefixUrl=Url.parse(this.filterPrefix,this.getCharset());
        for(int i=0;i<filterPrefixUrl.getSegments().size()-1;++i){
            buffer.prepend("../");
        }
        return buffer.toString();
    }
    public Charset getCharset(){
        return RequestUtils.getCharset(this.httpServletRequest);
    }
    public HttpServletRequest getContainerRequest(){
        return this.httpServletRequest;
    }
    public String getContextPath(){
        return UrlUtils.normalizePath(this.httpServletRequest.getContextPath());
    }
    public String getFilterPath(){
        return UrlUtils.normalizePath(this.filterPrefix);
    }
    public boolean shouldPreserveClientUrl(){
        return (this.errorAttributes!=null&&!Strings.isEmpty((CharSequence)this.errorAttributes.getRequestUri()))||(this.forwardAttributes!=null&&!Strings.isEmpty((CharSequence)this.forwardAttributes.getRequestUri()));
    }
    static{
        LOG=LoggerFactory.getLogger(ServletWebRequest.class);
        logger=LoggerFactory.getLogger(ServletWebRequest.class);
    }
}
