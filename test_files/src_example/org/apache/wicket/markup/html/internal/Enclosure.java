package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class Enclosure extends WebMarkupContainer implements IComponentResolver{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private Component childComponent;
    private final CharSequence childId;
    public Enclosure(final String id,final CharSequence childId){
        super(id);
        if(childId==null){
            throw new MarkupException("You most likely forgot to register the EnclosureHandler with the MarkupParserFactory");
        }
        this.childId=childId;
    }
    public final String getChildId(){
        return this.childId.toString();
    }
    protected void onInitialize(){
        super.onInitialize();
        this.childComponent=this.getChildComponent(new MarkupStream(this.getMarkup()),this.getEnclosureParent());
    }
    protected final Component getChild(){
        return this.childComponent;
    }
    public boolean isVisible(){
        return this.childComponent.determineVisibility()&&super.isVisible();
    }
    protected MarkupContainer getEnclosureParent(){
        MarkupContainer parent;
        for(parent=this.getParent();parent!=null&&parent.isAuto();parent=parent.getParent()){
        }
        if(parent==null){
            throw new WicketRuntimeException("Unable to find parent component which is not a transparent resolver");
        }
        return parent;
    }
    private Component getChildComponent(final MarkupStream markupStream,final MarkupContainer enclosureParent){
        String fullChildId=this.getChildId();
        Component controller=enclosureParent.get(fullChildId);
        if(controller==null){
            final int orgIndex=markupStream.getCurrentIndex();
            try{
                while(markupStream.hasMore()){
                    markupStream.next();
                    if(markupStream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)){
                        final ComponentTag tag=markupStream.getTag();
                        if(tag==null||(!tag.isOpen()&&!tag.isOpenClose())){
                            continue;
                        }
                        final String tagId=tag.getId();
                        if(fullChildId.equals(tagId)){
                            final ComponentTag fullComponentTag=new ComponentTag(tag);
                            fullComponentTag.setId(this.childId.toString());
                            controller=ComponentResolvers.resolve(enclosureParent,markupStream,fullComponentTag,new ComponentResolvers.ResolverFilter(){
                                public boolean ignoreResolver(final IComponentResolver resolver){
                                    return resolver instanceof EnclosureHandler;
                                }
                            });
                            break;
                        }
                        if(!fullChildId.startsWith(tagId+':')){
                            continue;
                        }
                        fullChildId=Strings.afterFirst(fullChildId,':');
                    }
                }
            }
            finally{
                markupStream.setCurrentIndex(orgIndex);
            }
        }
        this.checkChildComponent(controller);
        return controller;
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(this.childId.equals(tag.getId())){
            return this.childComponent;
        }
        return this.getEnclosureParent().get(tag.getId());
    }
    private void checkChildComponent(final Component controller){
        if(controller==null){
            throw new WicketRuntimeException("Could not find child with id: "+(Object)this.childId+" in the wicket:enclosure");
        }
        if(controller==this){
            throw new WicketRuntimeException("Programming error: childComponent == enclose component; endless loop");
        }
    }
    static{
        log=LoggerFactory.getLogger(Enclosure.class);
    }
}
