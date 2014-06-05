package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.util.visit.*;

public class ChildFirstHeaderRenderStrategy extends AbstractHeaderRenderStrategy{
    public void renderHeader(final HtmlHeaderContainer headerContainer,final Component rootComponent){
        Args.notNull((Object)headerContainer,"headerContainer");
        Args.notNull((Object)rootComponent,"rootComponent");
        this.renderApplicationLevelHeaders(headerContainer);
        this.renderChildHeaders(headerContainer,rootComponent);
        this.renderRootComponent(headerContainer,rootComponent);
    }
    protected void renderChildHeaders(final HtmlHeaderContainer headerContainer,final Component rootComponent){
        Args.notNull((Object)headerContainer,"headerContainer");
        Args.notNull((Object)rootComponent,"rootComponent");
        if(rootComponent instanceof MarkupContainer){
            new DeepChildFirstVisitor(){
                public void component(final Component component,final IVisit<Void> visit){
                    component.renderHead(headerContainer);
                }
                public boolean preCheck(final Component component){
                    return component.isVisibleInHierarchy();
                }
            }.visit(rootComponent);
        }
    }
}
