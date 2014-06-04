package org.apache.wicket.request.mapper;

import org.apache.wicket.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.mapper.info.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.util.lang.*;

public abstract class AbstractComponentMapper extends AbstractMapper implements IRequestMapper{
    protected IMapperContext getContext(){
        return Application.get().getMapperContext();
    }
    protected String requestListenerInterfaceToString(final RequestListenerInterface listenerInterface){
        Args.notNull((Object)listenerInterface,"listenerInterface");
        return this.getContext().requestListenerInterfaceToString(listenerInterface);
    }
    protected RequestListenerInterface requestListenerInterfaceFromString(final String interfaceName){
        Args.notEmpty((CharSequence)interfaceName,"interfaceName");
        return this.getContext().requestListenerInterfaceFromString(interfaceName);
    }
    protected PageComponentInfo getPageComponentInfo(final Url url){
        if(url==null){
            throw new IllegalStateException("Argument 'url' may not be null.");
        }
        for(final Url.QueryParameter queryParameter : url.getQueryParameters()){
            if(Strings.isEmpty((CharSequence)queryParameter.getValue())){
                final PageComponentInfo pageComponentInfo=PageComponentInfo.parse(queryParameter.getName());
                if(pageComponentInfo!=null){
                    return pageComponentInfo;
                }
                continue;
            }
        }
        return null;
    }
    protected void encodePageComponentInfo(final Url url,final PageComponentInfo info){
        Args.notNull((Object)url,"url");
        if(info!=null){
            final String s=info.toString();
            if(!Strings.isEmpty((CharSequence)s)){
                final Url.QueryParameter parameter=new Url.QueryParameter(s,"");
                url.getQueryParameters().add(parameter);
            }
        }
    }
    protected Class<? extends IRequestablePage> getPageClass(final String name){
        Args.notEmpty((CharSequence)name,"name");
        return WicketObjects.resolveClass(name);
    }
    protected void removeMetaParameter(final Url urlCopy){
        final String pageComponentInfoCandidate=((Url.QueryParameter)urlCopy.getQueryParameters().get(0)).getName();
        if(PageComponentInfo.parse(pageComponentInfoCandidate)!=null){
            urlCopy.getQueryParameters().remove(0);
        }
    }
}
