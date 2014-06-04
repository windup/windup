package org.apache.wicket.markup.html.internal;

import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.handler.resource.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.*;
import org.apache.wicket.response.*;
import org.slf4j.*;

public abstract class HeaderResponse implements IHeaderResponse{
    private static final Logger logger;
    private final Set<Object> rendered;
    private boolean closed;
    public HeaderResponse(){
        super();
        this.rendered=(Set<Object>)new HashSet();
    }
    public final void markRendered(final Object object){
        this.rendered.add(object);
    }
    public void renderCSS(final CharSequence css,final String id){
        Args.notNull((Object)css,"css");
        if(!this.closed){
            final List<String> token=(List<String>)Arrays.asList(new String[] { css.toString(),id });
            if(!this.wasRendered(token)){
                this.renderString((CharSequence)("<style type=\"text/css\"><!--\n"+(Object)css+"--></style>\n"));
                this.markRendered(token);
            }
        }
    }
    public void renderCSSReference(final ResourceReference reference){
        this.renderCSSReference(reference,null,null);
    }
    public void renderCSSReference(final ResourceReference reference,final String media){
        this.renderCSSReference(reference,null,media);
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media){
        this.renderCSSReference(reference,pageParameters,media,null);
    }
    public void renderCSSReference(final ResourceReference reference,final PageParameters pageParameters,final String media,final String condition){
        Args.notNull((Object)reference,"reference");
        if(!this.closed){
            final IRequestHandler handler=(IRequestHandler)new ResourceReferenceRequestHandler(reference,pageParameters);
            final CharSequence url=RequestCycle.get().urlFor(handler);
            this.internalRenderCSSReference(url.toString(),media,condition);
        }
    }
    public void renderCSSReference(final String url){
        this.renderCSSReference(url,null);
    }
    public void renderCSSReference(final String url,final String media){
        this.renderCSSReference(url,media,null);
    }
    public void renderCSSReference(final String url,final String media,final String condition){
        this.internalRenderCSSReference(this.relative(url),media,condition);
    }
    private void internalRenderCSSReference(final String url,final String media,final String condition){
        Args.notEmpty((CharSequence)url,"url");
        if(!this.closed){
            final List<String> token=(List<String>)Arrays.asList(new String[] { "css",url,media });
            if(!this.wasRendered(token)){
                if(!Strings.isEmpty((CharSequence)condition)){
                    if(AjaxRequestTarget.get()!=null){
                        HeaderResponse.logger.warn("IE CSS engine doesn't support dynamically injected links in conditional comments. See the javadoc of IHeaderResponse for alternative solution.");
                    }
                    this.getResponse().write((CharSequence)"<!--[if ");
                    this.getResponse().write((CharSequence)condition);
                    this.getResponse().write((CharSequence)"]>");
                }
                this.getResponse().write((CharSequence)"<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                this.getResponse().write(Strings.escapeMarkup((CharSequence)url));
                this.getResponse().write((CharSequence)"\"");
                if(media!=null){
                    this.getResponse().write((CharSequence)" media=\"");
                    this.getResponse().write(Strings.escapeMarkup((CharSequence)media));
                    this.getResponse().write((CharSequence)"\"");
                }
                this.getResponse().write((CharSequence)" />");
                if(!Strings.isEmpty((CharSequence)condition)){
                    this.getResponse().write((CharSequence)"<![endif]-->");
                }
                this.getResponse().write((CharSequence)"\n");
                this.markRendered(token);
            }
        }
    }
    public void renderJavaScriptReference(final ResourceReference reference){
        this.renderJavaScriptReference(reference,null);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final String id){
        this.renderJavaScriptReference(reference,null,id);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id){
        this.renderJavaScriptReference(reference,pageParameters,id,false);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id,final boolean defer){
        this.renderJavaScriptReference(reference,pageParameters,id,defer,null);
    }
    public void renderJavaScriptReference(final ResourceReference reference,final PageParameters pageParameters,final String id,final boolean defer,final String charset){
        Args.notNull((Object)reference,"reference");
        if(!this.closed){
            final IRequestHandler handler=(IRequestHandler)new ResourceReferenceRequestHandler(reference,pageParameters);
            final CharSequence url=RequestCycle.get().urlFor(handler);
            this.internalRenderJavaScriptReference(url.toString(),id,defer,charset);
        }
    }
    public void renderJavaScriptReference(final String url){
        this.renderJavaScriptReference(url,null);
    }
    public void renderJavaScriptReference(final String url,final String id){
        this.renderJavaScriptReference(url,id,false);
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer){
        this.renderJavaScriptReference(url,id,defer,null);
    }
    public void renderJavaScriptReference(final String url,final String id,final boolean defer,final String charset){
        this.internalRenderJavaScriptReference(this.relative(url),id,defer,charset);
    }
    private void internalRenderJavaScriptReference(final String url,final String id,final boolean defer,final String charset){
        Args.notEmpty((CharSequence)url,"url");
        if(!this.closed){
            final List<String> token1=(List<String>)Arrays.asList(new String[] { "javascript",url });
            final List<String> token2=(List<String>)((id!=null)?Arrays.asList(new String[] { "javascript",id }):null);
            final boolean token1Unused=!this.wasRendered(token1);
            final boolean token2Unused=token2==null||!this.wasRendered(token2);
            if(token1Unused&&token2Unused){
                final boolean isAjax=AjaxRequestTarget.get()!=null;
                final CharSequence escapedUrl=(CharSequence)(isAjax?Strings.escapeMarkup((CharSequence)url):url);
                JavaScriptUtils.writeJavaScriptUrl(this.getResponse(),escapedUrl,id,defer,charset);
                this.markRendered(token1);
                if(token2!=null){
                    this.markRendered(token2);
                }
            }
        }
    }
    public void renderJavaScript(final CharSequence javascript,final String id){
        Args.notNull((Object)javascript,"javascript");
        if(!this.closed){
            final List<String> token=(List<String>)Arrays.asList(new String[] { javascript.toString(),id });
            if(!this.wasRendered(token)){
                JavaScriptUtils.writeJavaScript(this.getResponse(),javascript,id);
                this.markRendered(token);
            }
        }
    }
    public void renderString(final CharSequence string){
        Args.notNull((Object)string,"string");
        if(!this.closed){
            final String token=string.toString();
            if(!this.wasRendered(token)){
                this.getResponse().write(string);
                this.markRendered(token);
            }
        }
    }
    public final boolean wasRendered(final Object object){
        return this.rendered.contains(object);
    }
    public void renderOnDomReadyJavaScript(final String javascript){
        if(javascript==null){
            throw new IllegalArgumentException("javascript cannot be null");
        }
        if(!this.closed){
            this.renderOnEventJavaScript("window","domready",javascript);
        }
    }
    public void renderOnLoadJavaScript(final String javascript){
        if(javascript==null){
            throw new IllegalArgumentException("javascript cannot be null");
        }
        if(!this.closed){
            this.renderOnEventJavaScript("window","load",javascript);
        }
    }
    public void renderOnEventJavaScript(final String target,final String event,final String javascript){
        if(!this.closed){
            final List<String> token=(List<String>)Arrays.asList(new String[] { "javascript-event",target,event,javascript });
            if(!this.wasRendered(token)){
                this.renderJavaScriptReference(WicketEventReference.INSTANCE);
                JavaScriptUtils.writeJavaScript(this.getResponse(),(CharSequence)("Wicket.Event.add("+target+", \""+event+"\", function(event) { "+javascript+";});"));
                this.markRendered(token);
            }
        }
    }
    public void close(){
        this.closed=true;
    }
    public final Response getResponse(){
        return this.closed?NullResponse.getInstance():this.getRealResponse();
    }
    public boolean isClosed(){
        return this.closed;
    }
    private String relative(final String url){
        Args.notEmpty((CharSequence)url,"location");
        if(url.startsWith("http://")||url.startsWith("https://")||url.startsWith("/")){
            return url;
        }
        final RequestCycle rc=RequestCycle.get();
        return rc.getUrlRenderer().renderContextRelativeUrl(url);
    }
    protected abstract Response getRealResponse();
    static{
        logger=LoggerFactory.getLogger(HeaderResponse.class);
    }
}
