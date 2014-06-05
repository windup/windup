package org.apache.wicket.protocol.http;

import org.apache.wicket.ajax.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.util.visit.*;
import java.util.*;

public class AjaxEnclosureListener implements AjaxRequestTarget.IListener{
    public void onBeforeRespond(final Map<String,Component> map,final AjaxRequestTarget target){
        final List<Component> originalComponents=(List<Component>)Collections.unmodifiableList(new ArrayList(map.values()));
        target.getPage().visitChildren((Class<?>)InlineEnclosure.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<InlineEnclosure,Void>(){
            public void component(final InlineEnclosure enclosure,final IVisit<Void> visit){
                for(final Component component : originalComponents){
                    if(AjaxEnclosureListener.this.isControllerOfEnclosure(component,enclosure)){
                        enclosure.updateVisibility();
                        target.add(enclosure);
                    }
                }
            }
        });
    }
    private boolean isControllerOfEnclosure(final Component component,final InlineEnclosure enclosure){
        return enclosure.getParent().get(enclosure.getChildId())==component;
    }
    public void onAfterRespond(final Map<String,Component> map,final AjaxRequestTarget.IJavaScriptResponse response){
    }
}
