package org.apache.wicket.markup.resolver;

import org.slf4j.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.interpolator.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.response.*;
import org.apache.wicket.request.*;

public class WicketMessageResolver implements IComponentResolver{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    public static final String MESSAGE="message";
    private static final String DEFAULT_VALUE="DEFAULT_WICKET_MESSAGE_RESOLVER_VALUE";
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag instanceof WicketTag){
            final WicketTag wtag=(WicketTag)tag;
            if(wtag.isMessageTag()){
                final String messageKey=wtag.getAttributes().getString("key");
                if(messageKey==null||messageKey.trim().length()==0){
                    throw new MarkupException("Wrong format of <wicket:message key='xxx'>: attribute 'key' is missing");
                }
                final String id="_message_"+container.getPage().getAutoIndex();
                final MessageContainer label=new MessageContainer(id,messageKey);
                label.setRenderBodyOnly(container.getApplication().getMarkupSettings().getStripWicketTags());
                return label;
            }
        }
        return null;
    }
    private static boolean isThrowExceptionIfPropertyNotFound(){
        return Application.get().getResourceSettings().getThrowExceptionOnMissingResource();
    }
    static{
        log=LoggerFactory.getLogger(WicketMessageResolver.class);
        WicketTagIdentifier.registerWellKnownTagName("message");
    }
    private static class MessageContainer extends MarkupContainer implements IComponentResolver{
        private static final long serialVersionUID=1L;
        private static final String NOT_FOUND="[Warning: Property for '%s' not found]";
        public MessageContainer(final String id,final String messageKey){
            super(id,new Model<Object>(messageKey));
            this.setEscapeModelStrings(false);
        }
        public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
            return this.getParent().get(tag.getId());
        }
        protected void onComponentTag(final ComponentTag tag){
            if(tag.isOpenClose()){
                tag.setType(XmlTag.TagType.OPEN);
            }
            super.onComponentTag(tag);
        }
        public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
            final String key=this.getDefaultModelObjectAsString();
            final String value=this.getLocalizer().getString(key,this.getParent(),"DEFAULT_WICKET_MESSAGE_RESOLVER_VALUE");
            if(value!=null&&!"DEFAULT_WICKET_MESSAGE_RESOLVER_VALUE".equals(value)){
                this.renderMessage(markupStream,openTag,key,value);
            }
            else{
                if(isThrowExceptionIfPropertyNotFound()){
                    throw new WicketRuntimeException("Property '"+key+"' not found in property files. Markup: "+markupStream.toString());
                }
                WicketMessageResolver.log.warn("No value found for wicket:message tag with key: {}",key);
                if(!markupStream.hasMore()){
                    final String formatedNotFound=String.format("[Warning: Property for '%s' not found]",new Object[] { key });
                    this.getResponse().write((CharSequence)formatedNotFound);
                }
                super.onComponentTagBody(markupStream,openTag);
            }
        }
        private void renderMessage(final MarkupStream markupStream,final ComponentTag openTag,final String key,final String value){
            final Map<String,CharSequence> childTags=this.findAndRenderChildWicketTags(markupStream,openTag);
            final Map<String,Object> variablesReplaced=(Map<String,Object>)new HashMap();
            final String text=new MapVariableInterpolator(value,childTags){
                protected String getValue(final String variableName){
                    String value=super.getValue(variableName);
                    if(value!=null){
                        variablesReplaced.put(variableName,null);
                    }
                    if(value==null){
                        value=Strings.toString(PropertyResolver.getValue(variableName,MessageContainer.this.getParent().getDefaultModelObject()));
                    }
                    if(value==null){
                        value=Strings.toString(PropertyResolver.getValue(variableName,MessageContainer.this.getParent()));
                    }
                    if(value==null){
                        final String msg="The localized text for <wicket:message key=\""+key+"\"> has a variable ${"+variableName+"}. However the wicket:message element does not have a child "+"element with a wicket:id=\""+variableName+"\".";
                        if(isThrowExceptionIfPropertyNotFound()){
                            markupStream.throwMarkupException(msg);
                        }
                        else{
                            WicketMessageResolver.log.warn(msg);
                            value="### VARIABLE NOT FOUND: "+variableName+" ###";
                        }
                    }
                    return value;
                }
            }.toString();
            this.getResponse().write((CharSequence)text);
            for(final String id : childTags.keySet()){
                if(!variablesReplaced.containsKey(id)){
                    final String msg="The <wicket:message key=\""+key+"\"> has a child element with wicket:id=\""+id+"\". You must add the variable ${"+id+"} to the localized text for the wicket:message.";
                    if(isThrowExceptionIfPropertyNotFound()){
                        markupStream.throwMarkupException(msg);
                    }
                    else{
                        WicketMessageResolver.log.warn(msg);
                    }
                }
            }
        }
        private Map<String,CharSequence> findAndRenderChildWicketTags(final MarkupStream markupStream,final ComponentTag openTag){
            final Map<String,CharSequence> childTags=(Map<String,CharSequence>)new HashMap();
            final ComponentTag tag=markupStream.getPreviousTag();
            if(!tag.isOpenClose()){
                while(markupStream.hasMore()&&!markupStream.get().closes(openTag)){
                    final MarkupElement element=markupStream.get();
                    if(element instanceof ComponentTag&&!markupStream.atCloseTag()){
                        final ComponentTag currentTag=(ComponentTag)element;
                        final String id=currentTag.getId();
                        final Response webResponse=this.getResponse();
                        try{
                            final StringResponse response=new StringResponse();
                            this.getRequestCycle().setResponse(response);
                            Component component=this.getParent().get(id);
                            if(component==null){
                                component=ComponentResolvers.resolve(this.getParent(),markupStream,currentTag,null);
                                if(component.getParent()==null){
                                    component=null;
                                }
                            }
                            if(component!=null){
                                component.render();
                                markupStream.skipComponent();
                            }
                            else{
                                markupStream.next();
                            }
                            childTags.put(id,response.getBuffer());
                        }
                        finally{
                            this.getRequestCycle().setResponse(webResponse);
                        }
                    }
                    else{
                        markupStream.next();
                    }
                }
            }
            return childTags;
        }
    }
}
