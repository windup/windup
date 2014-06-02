package org.apache.wicket.markup.html.panel;

import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.*;

public abstract class AssociatedMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy{
    private boolean noMoreWicketHeadTagsAllowed;
    private final String tagName;
    public AssociatedMarkupSourcingStrategy(final String tagName){
        super();
        this.noMoreWicketHeadTagsAllowed=false;
        this.tagName=(String)Args.notNull((Object)tagName,"tagName");
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
        super.onComponentTag(component,tag);
    }
    protected final void renderAssociatedMarkup(final Component component){
        ((MarkupContainer)component).renderAssociatedMarkup(this.tagName,"Markup for a "+this.tagName+" component must begin a tag like '<wicket:"+this.tagName+">'");
    }
    public IMarkupFragment getMarkup(final MarkupContainer parent,final Component child){
        Args.notNull((Object)this.tagName,"tagName");
        IMarkupFragment associatedMarkup=parent.getAssociatedMarkup();
        if(associatedMarkup==null){
            throw new MarkupNotFoundException("Failed to find markup file associated. "+parent.getClass().getSimpleName()+": "+parent.toString());
        }
        final IMarkupFragment markup=this.findStartTag(associatedMarkup);
        if(markup==null){
            throw new MarkupNotFoundException("Expected to find <wicket:"+this.tagName+"> in associated markup file. Markup: "+associatedMarkup.toString());
        }
        if(child==null){
            return markup;
        }
        associatedMarkup=markup.find(child.getId());
        if(associatedMarkup!=null){
            return associatedMarkup;
        }
        associatedMarkup=this.searchMarkupInTransparentResolvers(parent,child);
        if(associatedMarkup!=null){
            return associatedMarkup;
        }
        return this.findMarkupInAssociatedFileHeader(parent,child);
    }
    private final IMarkupFragment findStartTag(final IMarkupFragment markup){
        final MarkupStream stream=new MarkupStream(markup);
        while(stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)){
            final ComponentTag tag=stream.getTag();
            if(tag.isOpen()||tag.isOpenClose()){
                if(tag instanceof WicketTag){
                    final WicketTag wtag=(WicketTag)tag;
                    if(this.tagName.equalsIgnoreCase(wtag.getName())){
                        return stream.getMarkupFragment();
                    }
                }
                stream.skipToMatchingCloseTag(tag);
            }
            stream.next();
        }
        return null;
    }
    public IMarkupFragment findMarkupInAssociatedFileHeader(final MarkupContainer container,final Component child){
        final IMarkupFragment markup=container.getAssociatedMarkup();
        IMarkupFragment childMarkup=null;
        final MarkupStream stream=new MarkupStream(markup);
        while(stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)&&childMarkup==null){
            final ComponentTag tag=stream.getTag();
            if(TagUtils.isWicketHeadTag(tag)){
                if(tag.getMarkupClass()==null){
                    childMarkup=stream.getMarkupFragment().find(child.getId());
                }
            }
            else if(TagUtils.isHeadTag(tag)){
                childMarkup=stream.getMarkupFragment().find(child.getId());
            }
            if(tag.isOpen()&&!tag.hasNoCloseTag()){
                stream.skipToMatchingCloseTag(tag);
            }
            stream.next();
        }
        return childMarkup;
    }
    public void renderHead(final Component component,final HtmlHeaderContainer container){
        if(!(component instanceof WebMarkupContainer)){
            throw new WicketRuntimeException(component.getClass().getSimpleName()+" can only be associated with WebMarkupContainer.");
        }
        this.renderHeadFromAssociatedMarkupFile((WebMarkupContainer)component,container);
    }
    public final void renderHeadFromAssociatedMarkupFile(final WebMarkupContainer container,final HtmlHeaderContainer htmlContainer){
        this.noMoreWicketHeadTagsAllowed=false;
        final MarkupStream markupStream=container.getAssociatedMarkupStream(false);
        if(markupStream==null){
            return;
        }
        this.noMoreWicketHeadTagsAllowed=false;
        while(this.nextHeaderMarkup(markupStream)!=-1){
            final String headerId=this.getHeaderId(container,markupStream);
            final HeaderPartContainer headerPart=this.getHeaderPart(container,headerId,markupStream.getMarkupFragment());
            if(headerPart!=null&&htmlContainer.okToRenderComponent(headerPart.getScope(),headerPart.getId())){
                headerPart.setParent(htmlContainer);
                headerPart.render();
            }
            markupStream.skipComponent();
        }
    }
    private String getHeaderId(final Component container,final MarkupStream markupStream){
        Class<?> markupClass=markupStream.getTag().getMarkupClass();
        if(markupClass==null){
            markupClass=markupStream.getContainerClass();
        }
        final StringBuilder builder=new StringBuilder(100);
        builder.append("_");
        builder.append(Classes.simpleName((Class)markupClass));
        if(container.getVariation()!=null){
            builder.append(container.getVariation());
        }
        builder.append("Header");
        builder.append(markupStream.getCurrentIndex());
        return builder.toString();
    }
    private final HeaderPartContainer getHeaderPart(final WebMarkupContainer container,final String id,final IMarkupFragment markup){
        final MarkupElement element=markup.get(0);
        if(element instanceof WicketTag){
            final WicketTag wTag=(WicketTag)element;
            if(wTag.isHeadTag()&&wTag.getNamespace()!=null){
                return new HeaderPartContainer(id,container,markup);
            }
        }
        throw new WicketRuntimeException("Programming error: expected a WicketTag: "+markup.toString());
    }
    private final int nextHeaderMarkup(final MarkupStream associatedMarkupStream){
        if(associatedMarkupStream==null){
            return -1;
        }
        for(MarkupElement elem=associatedMarkupStream.get();elem!=null;elem=associatedMarkupStream.next()){
            if(elem instanceof WicketTag){
                final WicketTag tag=(WicketTag)elem;
                if(tag.isOpen()&&tag.isHeadTag()){
                    if(this.noMoreWicketHeadTagsAllowed){
                        throw new MarkupException("<wicket:head> tags are only allowed before <body>, </head>, <wicket:panel> etc. tag");
                    }
                    return associatedMarkupStream.getCurrentIndex();
                }
                else if(tag.isOpen()&&(tag.isPanelTag()||tag.isBorderTag()||tag.isExtendTag())){
                    this.noMoreWicketHeadTagsAllowed=true;
                }
            }
            else if(elem instanceof ComponentTag){
                final ComponentTag tag2=(ComponentTag)elem;
                if(tag2.isClose()&&TagUtils.isHeadTag(tag2)){
                    this.noMoreWicketHeadTagsAllowed=true;
                }
                else if(tag2.isOpen()&&TagUtils.isBodyTag(tag2)){
                    this.noMoreWicketHeadTagsAllowed=true;
                }
            }
        }
        return -1;
    }
}
