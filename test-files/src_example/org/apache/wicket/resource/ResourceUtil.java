package org.apache.wicket.resource;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.resource.aggregation.*;
import org.apache.wicket.resource.dependencies.*;
import org.apache.wicket.request.mapper.parameter.*;
import java.nio.charset.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;
import java.io.*;

public class ResourceUtil{
    @Deprecated
    public static void renderTo(final IHeaderResponse resp,final ResourceReference ref,final boolean css,final String string){
        if(css){
            if(Strings.isEmpty((CharSequence)string)){
                resp.renderCSSReference(ref);
            }
            else{
                resp.renderCSSReference(ref,string);
            }
        }
        else if(Strings.isEmpty((CharSequence)string)){
            resp.renderJavaScriptReference(ref);
        }
        else{
            resp.renderJavaScriptReference(ref,string);
        }
    }
    public static void renderTo(final IHeaderResponse resp,final ResourceReferenceAndStringData data){
        final AbstractResourceDependentResourceReference.ResourceType resourceType=data.getResourceType();
        final ResourceReference reference=data.getReference();
        final PageParameters parameters=data.getParameters();
        final String idOrMedia=data.getIdOrMedia();
        final CharSequence content=data.getContent();
        switch(resourceType){
            case CSS:{
                final String condition=data.getCssCondition();
                if(!Strings.isEmpty(content)){
                    resp.renderCSS(content,idOrMedia);
                    break;
                }
                if(reference==null){
                    resp.renderCSSReference(data.getUrl(),idOrMedia,condition);
                    break;
                }
                resp.renderCSSReference(reference,parameters,idOrMedia,condition);
                break;
            }
            case JS:{
                final boolean defer=data.isJsDefer();
                final String charset=data.getCharset();
                if(!Strings.isEmpty(content)){
                    resp.renderJavaScript(content,idOrMedia);
                    break;
                }
                if(reference==null){
                    resp.renderJavaScriptReference(data.getUrl(),idOrMedia,defer,charset);
                    break;
                }
                resp.renderJavaScriptReference(reference,parameters,idOrMedia,defer,charset);
                break;
            }
            case PLAIN:{
                resp.renderString(content);
                break;
            }
        }
    }
    public static String readString(final IResourceStream resourceStream){
        return readString(resourceStream,null);
    }
    public static String readString(final IResourceStream resourceStream,Charset charset){
        try{
            final InputStream stream=resourceStream.getInputStream();
            try{
                final byte[] bytes=IOUtils.toByteArray(stream);
                if(charset==null){
                    charset=Charset.defaultCharset();
                }
                return new String(bytes,charset.name());
            }
            finally{
                resourceStream.close();
            }
        }
        catch(IOException e){
            throw new WicketRuntimeException("failed to read string from "+resourceStream,e);
        }
        catch(ResourceStreamNotFoundException e2){
            throw new WicketRuntimeException("failed to locate stream from "+resourceStream,(Throwable)e2);
        }
    }
}
