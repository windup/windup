package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.util.visit.*;

public class ParentFirstHeaderRenderStrategy extends AbstractHeaderRenderStrategy{
    protected void renderChildHeaders(final HtmlHeaderContainer headerContainer,final Component rootComponent){
        Args.notNull((Object)headerContainer,"headerContainer");
        Args.notNull((Object)rootComponent,"rootComponent");
        if(rootComponent instanceof MarkupContainer){
            ((MarkupContainer)rootComponent).visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    if(component.isVisibleInHierarchy()){
                        component.renderHead(headerContainer);
                    }
                    else{
                        visit.dontGoDeeper();
                    }
                }
            });
        }
    }
}
