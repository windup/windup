package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.util.visit.*;
import org.slf4j.*;
import org.apache.wicket.markup.html.*;

public class AutoLabelResolver implements IComponentResolver{
    private static final long serialVersionUID=1L;
    private static Logger logger;
    static final String WICKET_FOR="wicket:for";
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(!AutoLabelTagHandler.class.getName().equals(tag.getId())){
            return null;
        }
        final String id=tag.getAttribute("wicket:for").trim();
        final Component component=findRelatedComponent(container,id);
        if(component==null){
            throw new WicketRuntimeException("Could not find form component with id '"+id+"' while trying to resolve wicket:for attribute");
        }
        if(!(component instanceof ILabelProvider)){
            throw new WicketRuntimeException("Component pointed to by wicket:for attribute '"+id+"' does not implement "+ILabelProvider.class.getName());
        }
        if(!component.getOutputMarkupId()){
            component.setOutputMarkupId(true);
            if(component.hasBeenRendered()){
                AutoLabelResolver.logger.warn("Component: {} is referenced via a wicket:for attribute but does not have its outputMarkupId property set to true",component.toString(false));
            }
        }
        return new AutoLabel("label"+container.getPage().getAutoIndex(),component);
    }
    static Component findRelatedComponent(MarkupContainer container,final String id){
        Component component=container.get(id);
        if(component!=null){
            return component;
        }
        final Component[] searched= { null };
        while(container!=null){
            component=container.visitChildren((Class<?>)Component.class,(org.apache.wicket.util.visit.IVisitor<Component,Component>)new IVisitor<Component,Component>(){
                public void component(final Component child,final IVisit<Component> visit){
                    if(child==searched[0]){
                        visit.dontGoDeeper();
                        return;
                    }
                    if(id.equals(child.getId())){
                        visit.stop((Object)child);
                    }
                }
            });
            if(component!=null){
                return component;
            }
            searched[0]=container;
            container=container.getParent();
        }
        return null;
    }
    static{
        AutoLabelResolver.logger=LoggerFactory.getLogger(AutoLabelResolver.class);
    }
    protected static class AutoLabel extends TransparentWebMarkupContainer{
        private static final long serialVersionUID=1L;
        private final Component component;
        public AutoLabel(final String id,final Component fc){
            super(id);
            this.component=fc;
        }
        protected void onComponentTag(final ComponentTag tag){
            super.onComponentTag(tag);
            tag.put("for",(CharSequence)this.component.getMarkupId());
            if(this.component instanceof FormComponent){
                final FormComponent<?> fc=(FormComponent<?>)this.component;
                if(fc.isRequired()){
                    tag.append("class",(CharSequence)"required"," ");
                }
                if(!fc.isValid()){
                    tag.append("class",(CharSequence)"error"," ");
                }
            }
            if(!this.component.isEnabledInHierarchy()){
                tag.append("class",(CharSequence)"disabled"," ");
            }
        }
        public Component getRelatedComponent(){
            return this.component;
        }
    }
}
