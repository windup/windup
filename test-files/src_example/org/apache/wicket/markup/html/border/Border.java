package org.apache.wicket.markup.html.border;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.lang.*;

public abstract class Border extends WebMarkupContainer implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String BODY="body";
    public static final String BORDER="border";
    private final BorderBodyContainer body;
    public Border(final String id){
        this(id,null);
    }
    public Border(final String id,final IModel<?> model){
        super(id,model);
        this.body=new BorderBodyContainer(id+"_"+"body");
        this.addToBorder(this.body);
    }
    public final BorderBodyContainer getBodyContainer(){
        return this.body;
    }
    public Border add(final Component... children){
        this.getBodyContainer().add(children);
        return this;
    }
    public Border addOrReplace(final Component... children){
        this.getBodyContainer().addOrReplace(children);
        return this;
    }
    public Border remove(final Component component){
        if(component==this.body){
            super.remove(this.body);
        }
        else{
            this.getBodyContainer().remove(component);
        }
        return this;
    }
    public Border remove(final String id){
        this.getBodyContainer().remove(id);
        return this;
    }
    public Border removeAll(){
        this.getBodyContainer().removeAll();
        return this;
    }
    public Border replace(final Component replacement){
        this.getBodyContainer().replace(replacement);
        return this;
    }
    public Border addToBorder(final Component... children){
        super.add(children);
        return this;
    }
    public Border removeFromBorder(final Component child){
        super.remove(child);
        return this;
    }
    public Border replaceInBorder(final Component component){
        super.replace(component);
        return this;
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(!this.body.rendering&&TagUtils.isWicketBodyTag(tag)){
            return this.body;
        }
        return null;
    }
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy(){
        return new BorderMarkupSourcingStrategy();
    }
    public IMarkupFragment getMarkup(final Component child){
        final IMarkupFragment markup=this.getAssociatedMarkup();
        if(markup==null){
            throw new MarkupException("Unable to find associated markup file for Border: "+this.toString());
        }
        IMarkupFragment borderMarkup=null;
        for(int i=0;i<markup.size();++i){
            final MarkupElement elem=markup.get(i);
            if(TagUtils.isWicketBorderTag(elem)){
                borderMarkup=new MarkupFragment(markup,i);
                break;
            }
        }
        if(borderMarkup==null){
            throw new MarkupException((IResourceStream)markup.getMarkupResourceStream(),"Unable to find <wicket:border> tag in associated markup file for Border: "+this.toString());
        }
        if(child==null){
            return borderMarkup;
        }
        if(child==this.body){
            return this.body.getMarkup();
        }
        final IMarkupFragment childMarkup=borderMarkup.find(child.getId());
        if(childMarkup!=null){
            return childMarkup;
        }
        return ((BorderMarkupSourcingStrategy)this.getMarkupSourcingStrategy()).findMarkupInAssociatedFileHeader(this,child);
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("border");
        WicketTagIdentifier.registerWellKnownTagName("body");
    }
    public class BorderBodyContainer extends WebMarkupContainer{
        private static final long serialVersionUID=1L;
        private transient IMarkupFragment markup;
        protected boolean rendering;
        public BorderBodyContainer(final String id){
            super(id);
        }
        protected void onComponentTag(final ComponentTag tag){
            if(tag.isOpenClose()){
                tag.setType(XmlTag.TagType.OPEN);
                tag.setModified(true);
            }
            super.onComponentTag(tag);
        }
        public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
            if(markupStream.getPreviousTag().isOpen()){
                markupStream.skipRawMarkup();
            }
            final IMarkupFragment markup=Border.this.getMarkup();
            final MarkupStream stream=new MarkupStream(markup);
            final ComponentTag tag=stream.getTag();
            stream.next();
            super.onComponentTagBody(stream,tag);
        }
        protected void onRender(){
            this.rendering=true;
            try{
                super.onRender();
            }
            finally{
                this.rendering=false;
            }
        }
        public IMarkupFragment getMarkup(){
            if(this.markup==null){
                this.markup=this.findByName(this.getParent().getMarkup(null),"body");
            }
            return this.markup;
        }
        private final IMarkupFragment findByName(final IMarkupFragment markup,final String name){
            Args.notEmpty((CharSequence)name,"name");
            final MarkupStream stream=new MarkupStream(markup);
            stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class);
            stream.next();
            while(stream.skipUntil((Class<? extends MarkupElement>)ComponentTag.class)){
                final ComponentTag tag=stream.getTag();
                if((tag.isOpen()||tag.isOpenClose())&&TagUtils.isWicketBodyTag(tag)){
                    return stream.getMarkupFragment();
                }
                stream.next();
            }
            return null;
        }
        public IMarkupFragment getMarkup(final Component child){
            final IMarkupFragment markup=Border.this.getMarkup();
            if(markup==null){
                return null;
            }
            if(child==null){
                return markup;
            }
            return markup.find(child.getId());
        }
    }
}
