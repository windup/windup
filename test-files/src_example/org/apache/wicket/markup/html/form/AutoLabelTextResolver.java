package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.model.*;

public class AutoLabelTextResolver implements IComponentResolver{
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(!(tag instanceof WicketTag)||!"label".equals(((WicketTag)tag).getName())){
            return null;
        }
        Component related=null;
        if(tag.getAttribute("for")!=null){
            final Component component=related=AutoLabelResolver.findRelatedComponent(container,tag.getAttribute("for"));
        }
        if(related==null){
            if(container instanceof AutoLabelResolver.AutoLabel){
                related=((AutoLabelResolver.AutoLabel)container).getRelatedComponent();
            }
            if(related==null){
                final AutoLabelResolver.AutoLabel autoLabel=container.findParent((Class<AutoLabelResolver.AutoLabel>)AutoLabelResolver.AutoLabel.class);
                if(autoLabel!=null){
                    related=autoLabel.getRelatedComponent();
                }
            }
        }
        if(related==null){
            throw new IllegalStateException("no related component found for <wicket:label>");
        }
        return new TextLabel("label"+container.getPage().getAutoIndex(),related);
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("label");
    }
    private static class TextLabel extends WebMarkupContainer{
        private final Component labeled;
        public TextLabel(final String id,final Component labeled){
            super(id);
            this.labeled=labeled;
            this.setRenderBodyOnly(true);
        }
        protected void onComponentTag(final ComponentTag tag){
            if(tag.isOpenClose()){
                tag.setType(XmlTag.TagType.OPEN);
            }
            super.onComponentTag(tag);
        }
        public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
            final IModel<String> labelModel=this.findLabelContent(markupStream,openTag);
            this.replaceComponentTagBody(markupStream,openTag,(CharSequence)((labelModel!=null)?labelModel.getObject():""));
            if(labelModel!=null){
                if(this.labeled instanceof FormComponent){
                    final FormComponent<?> fc=(FormComponent<?>)this.labeled;
                    fc.setLabel(labelModel);
                }
                else{
                    labelModel.detach();
                }
            }
        }
        private IModel<String> findLabelContent(final MarkupStream markupStream,final ComponentTag tag){
            if(this.labeled instanceof ILabelProvider){
                final ILabelProvider<String> provider=(ILabelProvider<String>)this.labeled;
                if(provider.getLabel()!=null&&!Strings.isEmpty((CharSequence)provider.getLabel().getObject())){
                    return provider.getLabel();
                }
            }
            if(this.labeled instanceof FormComponent){
                final FormComponent<?> formComponent=(FormComponent<?>)this.labeled;
                final String text=formComponent.getDefaultLabel("wicket:unknown");
                if(!"wicket:unknown".equals(text)&&!Strings.isEmpty((CharSequence)text)){
                    return new LoadableDetachableModel<String>(){
                        protected String load(){
                            return formComponent.getDefaultLabel("wicket:unknown");
                        }
                    };
                }
            }
            final String resourceKey=tag.getAttribute("key");
            if(resourceKey!=null){
                final String text=this.labeled.getString(resourceKey);
                if(!Strings.isEmpty((CharSequence)text)){
                    return new StringResourceModel(resourceKey,this.labeled,null,new Object[0]);
                }
            }
            final String text2=new ResponseBufferZone(RequestCycle.get(),markupStream){
                protected void executeInsideBufferedZone(){
                    TextLabel.this.onComponentTagBody(markupStream,tag);
                }
            }.execute().toString();
            if(!Strings.isEmpty((CharSequence)text2)){
                return Model.of(text2);
            }
            return null;
        }
    }
}
