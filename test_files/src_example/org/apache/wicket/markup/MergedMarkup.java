package org.apache.wicket.markup;

import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class MergedMarkup extends Markup{
    private static final Logger log;
    public MergedMarkup(final Markup markup,final Markup baseMarkup,final int extendIndex){
        super(markup.getMarkupResourceStream());
        this.getMarkupResourceStream().setBaseMarkup(baseMarkup);
        final MarkupResourceStream baseResourceStream=baseMarkup.getMarkupResourceStream();
        this.getMarkupResourceStream().setEncoding(baseResourceStream.getEncoding());
        this.getMarkupResourceStream().setWicketNamespace(baseResourceStream.getWicketNamespace());
        if(MergedMarkup.log.isDebugEnabled()){
            final String derivedResource=Strings.afterLast(markup.getMarkupResourceStream().getResource().toString(),'/');
            final String baseResource=Strings.afterLast(baseMarkup.getMarkupResourceStream().getResource().toString(),'/');
            MergedMarkup.log.debug("Merge markup: derived markup: "+derivedResource+"; base markup: "+baseResource);
        }
        this.merge(markup,baseMarkup,extendIndex);
        if(MergedMarkup.log.isDebugEnabled()){
            MergedMarkup.log.debug("Merge markup: "+this.toString());
        }
    }
    public String locationAsString(){
        final String l1=this.getMarkupResourceStream().getBaseMarkup().locationAsString();
        final String l2=this.getMarkupResourceStream().locationAsString();
        if(l1==null&&l2==null){
            return null;
        }
        return l1+":"+l2;
    }
    private void merge(final IMarkupFragment markup,final IMarkupFragment baseMarkup,int extendIndex){
        boolean wicketHeadProcessed=false;
        boolean foundHeadTag=false;
        WicketTag childTag=null;
        int baseIndex;
        for(baseIndex=0;baseIndex<baseMarkup.size();++baseIndex){
            final MarkupElement element=baseMarkup.get(baseIndex);
            if(element instanceof RawMarkup){
                this.addMarkupElement(element);
            }
            else{
                final ComponentTag tag=(ComponentTag)element;
                if(baseMarkup.getMarkupResourceStream().getResource()!=null&&tag.getMarkupClass()==null){
                    tag.setMarkupClass(baseMarkup.getMarkupResourceStream().getMarkupClass());
                }
                if(element instanceof WicketTag){
                    final WicketTag wtag=(WicketTag)element;
                    if(wtag.isChildTag()&&tag.getMarkupClass()==baseMarkup.getMarkupResourceStream().getMarkupClass()){
                        if(wtag.isOpenClose()){
                            childTag=wtag;
                            final WicketTag childOpenTag=(WicketTag)wtag.mutable();
                            childOpenTag.getXmlTag().setType(XmlTag.TagType.OPEN);
                            childOpenTag.setMarkupClass(baseMarkup.getMarkupResourceStream().getMarkupClass());
                            this.addMarkupElement(childOpenTag);
                            break;
                        }
                        if(wtag.isOpen()){
                            this.addMarkupElement(wtag);
                            break;
                        }
                        throw new WicketRuntimeException("Did not expect a </wicket:child> tag in "+baseMarkup.toString());
                    }
                    else if(!wicketHeadProcessed){
                        if(wtag.isClose()&&wtag.isHeadTag()&&!foundHeadTag){
                            wicketHeadProcessed=true;
                            this.addMarkupElement(wtag);
                            this.copyWicketHead(markup,extendIndex);
                            continue;
                        }
                        if(wtag.isOpen()&&wtag.isMajorWicketComponentTag()){
                            wicketHeadProcessed=true;
                            this.copyWicketHead(markup,extendIndex);
                        }
                    }
                }
                if(!wicketHeadProcessed){
                    if(tag.isOpen()&&TagUtils.isHeadTag(tag)){
                        foundHeadTag=true;
                    }
                    if((tag.isClose()&&TagUtils.isHeadTag(tag))||(tag.isOpen()&&TagUtils.isBodyTag(tag))){
                        wicketHeadProcessed=true;
                        this.copyWicketHead(markup,extendIndex);
                    }
                }
                this.addMarkupElement(element);
            }
        }
        if(baseIndex==baseMarkup.size()){
            throw new WicketRuntimeException("Expected to find <wicket:child/> in base markup: "+baseMarkup.toString());
        }
        while(extendIndex<markup.size()){
            final MarkupElement element=markup.get(extendIndex);
            this.addMarkupElement(element);
            if(element instanceof WicketTag){
                final WicketTag wtag2=(WicketTag)element;
                if(wtag2.isExtendTag()&&wtag2.isClose()){
                    break;
                }
            }
            ++extendIndex;
        }
        if(extendIndex==markup.size()){
            throw new WicketRuntimeException("Missing close tag </wicket:extend> in derived markup: "+markup.toString());
        }
        if(((ComponentTag)baseMarkup.get(baseIndex)).isOpen()){
            ++baseIndex;
            while(baseIndex<baseMarkup.size()){
                final MarkupElement element=baseMarkup.get(baseIndex);
                if(element instanceof WicketTag){
                    final WicketTag tag2=(WicketTag)element;
                    if(tag2.isChildTag()&&tag2.isClose()){
                        tag2.setMarkupClass(baseMarkup.getMarkupResourceStream().getMarkupClass());
                        this.addMarkupElement(tag2);
                        break;
                    }
                    throw new WicketRuntimeException("Wicket tags like <wicket:xxx> are not allowed in between <wicket:child> and </wicket:child> tags: "+markup.toString());
                }
                else{
                    if(element instanceof ComponentTag){
                        throw new WicketRuntimeException("Wicket tags identified by wicket:id are not allowed in between <wicket:child> and </wicket:child> tags: "+markup.toString());
                    }
                    ++baseIndex;
                }
            }
            if(baseIndex==baseMarkup.size()){
                throw new WicketRuntimeException("Expected to find </wicket:child> in base markup: "+baseMarkup.toString());
            }
        }
        else{
            final WicketTag childCloseTag=(WicketTag)childTag.mutable();
            childCloseTag.getXmlTag().setType(XmlTag.TagType.CLOSE);
            childCloseTag.setMarkupClass(baseMarkup.getMarkupResourceStream().getMarkupClass());
            this.addMarkupElement(childCloseTag);
        }
        ++baseIndex;
        while(baseIndex<baseMarkup.size()){
            final MarkupElement element=baseMarkup.get(baseIndex);
            this.addMarkupElement(element);
            if(element instanceof ComponentTag&&baseMarkup.getMarkupResourceStream().getResource()!=null){
                final ComponentTag tag=(ComponentTag)element;
                tag.setMarkupClass(baseMarkup.getMarkupResourceStream().getMarkupClass());
            }
            ++baseIndex;
        }
        if(Page.class.isAssignableFrom(markup.getMarkupResourceStream().getMarkupClass())){
            int hasOpenWicketHead=-1;
            int hasCloseWicketHead=-1;
            int hasHead=-1;
            for(int i=0;i<this.size();++i){
                final MarkupElement element2=this.get(i);
                if(hasOpenWicketHead==-1&&element2 instanceof WicketTag&&((WicketTag)element2).isHeadTag()){
                    hasOpenWicketHead=i;
                }
                else if(element2 instanceof WicketTag&&((WicketTag)element2).isHeadTag()&&((WicketTag)element2).isClose()){
                    hasCloseWicketHead=i;
                }
                else if(hasHead==-1&&element2 instanceof ComponentTag&&TagUtils.isHeadTag(element2)){
                    hasHead=i;
                }
                else if(hasHead!=-1&&hasOpenWicketHead!=-1){
                    break;
                }
            }
            if(hasOpenWicketHead!=-1&&hasHead==-1){
                final XmlTag headOpenTag=new XmlTag();
                headOpenTag.setName("head");
                headOpenTag.setType(XmlTag.TagType.OPEN);
                final ComponentTag openTag=new ComponentTag(headOpenTag);
                openTag.setId("_header_");
                openTag.setAutoComponentTag(true);
                final XmlTag headCloseTag=new XmlTag();
                headCloseTag.setName(headOpenTag.getName());
                headCloseTag.setType(XmlTag.TagType.CLOSE);
                final ComponentTag closeTag=new ComponentTag(headCloseTag);
                closeTag.setOpenTag(openTag);
                closeTag.setId("_header_");
                this.addMarkupElement(hasOpenWicketHead,openTag);
                this.addMarkupElement(hasCloseWicketHead+2,closeTag);
            }
        }
    }
    private void copyWicketHead(final IMarkupFragment markup,final int extendIndex){
        boolean copy=false;
        for(int i=0;i<extendIndex;++i){
            final MarkupElement elem=markup.get(i);
            if(elem instanceof WicketTag){
                final WicketTag etag=(WicketTag)elem;
                if(etag.isHeadTag()){
                    if(!etag.isOpen()){
                        this.addMarkupElement(elem);
                        break;
                    }
                    copy=true;
                }
            }
            if(copy){
                this.addMarkupElement(elem);
            }
        }
    }
    static{
        log=LoggerFactory.getLogger(MergedMarkup.class);
    }
}
