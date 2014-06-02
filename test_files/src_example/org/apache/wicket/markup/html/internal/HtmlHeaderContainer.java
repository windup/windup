package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.response.*;
import org.apache.wicket.request.*;
import org.apache.wicket.markup.renderStrategy.*;
import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public class HtmlHeaderContainer extends TransparentWebMarkupContainer{
    private static final long serialVersionUID=1L;
    private transient Map<String,List<String>> renderedComponentsPerScope;
    private transient IHeaderResponse headerResponse;
    public HtmlHeaderContainer(final String id){
        super(id);
        this.headerResponse=null;
        this.setRenderBodyOnly(true);
        this.setAuto(true);
    }
    public final void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final Response webResponse=this.getResponse();
        try{
            final StringResponse response=new StringResponse();
            this.getRequestCycle().setResponse(response);
            final IHeaderResponse headerResponse=this.getHeaderResponse();
            if(!response.equals(headerResponse.getResponse())){
                this.getRequestCycle().setResponse(headerResponse.getResponse());
            }
            AbstractHeaderRenderStrategy.get().renderHeader(this,this.getPage());
            headerResponse.close();
            final StringResponse bodyResponse=new StringResponse();
            this.getRequestCycle().setResponse(bodyResponse);
            super.onComponentTagBody(markupStream,openTag);
            final CharSequence output=this.getCleanResponse(response);
            final CharSequence bodyOutput=this.getCleanResponse(bodyResponse);
            if(output.length()>0||bodyOutput.length()>0){
                if(this.renderOpenAndCloseTags()){
                    webResponse.write((CharSequence)"<head>");
                }
                webResponse.write(bodyOutput);
                webResponse.write(output);
                if(this.renderOpenAndCloseTags()){
                    webResponse.write((CharSequence)"</head>");
                }
            }
        }
        finally{
            this.getRequestCycle().setResponse(webResponse);
        }
    }
    private CharSequence getCleanResponse(final StringResponse response){
        CharSequence output=response.getBuffer();
        if(output.length()>0){
            if(output.charAt(0)=='\r'){
                for(int i=2;i<output.length();i+=2){
                    final char ch=output.charAt(i);
                    if(ch!='\r'){
                        output=output.subSequence(i-2,output.length());
                        break;
                    }
                }
            }
            else if(output.charAt(0)=='\n'){
                for(int i=1;i<output.length();++i){
                    final char ch=output.charAt(i);
                    if(ch!='\n'){
                        output=output.subSequence(i-1,output.length());
                        break;
                    }
                }
            }
        }
        return output;
    }
    protected boolean renderOpenAndCloseTags(){
        return true;
    }
    public boolean okToRenderComponent(final String scope,final String id){
        if(this.renderedComponentsPerScope==null){
            this.renderedComponentsPerScope=(Map<String,List<String>>)new HashMap();
        }
        List<String> componentScope=(List<String>)this.renderedComponentsPerScope.get(scope);
        if(componentScope==null){
            componentScope=(List<String>)new ArrayList();
            this.renderedComponentsPerScope.put(scope,componentScope);
        }
        if(componentScope.contains(id)){
            return false;
        }
        componentScope.add(id);
        return true;
    }
    protected void onDetach(){
        super.onDetach();
        this.renderedComponentsPerScope=null;
        this.headerResponse=null;
    }
    protected IHeaderResponse newHeaderResponse(){
        return new HeaderResponse(){
            protected Response getRealResponse(){
                return HtmlHeaderContainer.this.getResponse();
            }
        };
    }
    public IHeaderResponse getHeaderResponse(){
        if(this.headerResponse==null){
            this.headerResponse=this.getApplication().decorateHeaderResponse(this.newHeaderResponse());
        }
        return this.headerResponse;
    }
    public IMarkupFragment getMarkup(){
        if(this.getParent()==null){
            throw new WicketRuntimeException("Bug: The Wicket internal instance of HtmlHeaderContainer is not connected to a parent");
        }
        final IMarkupFragment markup=this.getPage().getMarkup();
        if(markup==null){
            throw new MarkupException("Unable to get page markup: "+this.getPage().toString());
        }
        final MarkupStream stream=new MarkupStream(markup);
        IMarkupFragment headerMarkup=null;
        while(stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)&&headerMarkup==null){
            final ComponentTag tag=stream.getTag();
            if(tag.isOpen()||tag.isOpenClose()){
                if(tag instanceof WicketTag){
                    final WicketTag wtag=(WicketTag)tag;
                    if(wtag.isHeadTag()&&tag.getMarkupClass()==null){
                        headerMarkup=stream.getMarkupFragment();
                    }
                }
                else if(tag.getName().equalsIgnoreCase("head")){
                    headerMarkup=stream.getMarkupFragment();
                }
            }
            stream.next();
        }
        this.setMarkup(headerMarkup);
        return headerMarkup;
    }
}
