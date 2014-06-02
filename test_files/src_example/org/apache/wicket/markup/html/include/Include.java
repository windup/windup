package org.apache.wicket.markup.html.include;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.*;
import javax.servlet.http.*;
import org.apache.wicket.*;
import java.net.*;
import java.nio.charset.*;
import org.apache.wicket.resource.*;
import org.apache.wicket.util.resource.*;

public class Include extends WebComponent{
    private static final long serialVersionUID=1L;
    private static final String VALID_SCHEME_CHARS="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";
    public Include(final String id){
        super(id);
    }
    public Include(final String id,final IModel<String> model){
        super(id,model);
    }
    public Include(final String id,final String modelObject){
        super(id,new Model<Object>(modelObject));
    }
    protected String importAsString(){
        final String url=this.getDefaultModelObjectAsString();
        if(UrlUtils.isRelative(url)){
            return this.importRelativeUrl((CharSequence)url);
        }
        return this.importAbsoluteUrl((CharSequence)url);
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final String content=this.importAsString();
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)content);
    }
    private String importRelativeUrl(final CharSequence url){
        final HttpServletRequest req=(HttpServletRequest)this.getRequest().getContainerRequest();
        final StringBuilder buildUrl=new StringBuilder(url.length());
        final String scheme=req.getScheme();
        final int port=req.getServerPort();
        buildUrl.append(scheme);
        buildUrl.append("://");
        buildUrl.append(req.getServerName());
        if((scheme.equals("http")&&port!=80)||(scheme.equals("https")&&port!=443)){
            buildUrl.append(':');
            buildUrl.append(req.getServerPort());
        }
        buildUrl.append(req.getContextPath()).append('/').append(url);
        return this.importAbsoluteUrl(buildUrl);
    }
    private String importAbsoluteUrl(final CharSequence url){
        try{
            return this.importUrl(new URL(url.toString()));
        }
        catch(MalformedURLException e){
            throw new WicketRuntimeException(e);
        }
    }
    public Charset getCharset(){
        return null;
    }
    private final String importUrl(final URL url){
        return ResourceUtil.readString((IResourceStream)new UrlResourceStream(url),this.getCharset());
    }
}
