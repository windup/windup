package org.apache.wicket.markup.html;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public class MarkupUtil{
    public static final boolean isMarkupHtml5Compliant(final MarkupContainer container){
        Args.notNull((Object)container,"container");
        final Page page=container.getPage();
        if(page==null){
            throw new WicketRuntimeException("Component not attached to Page. Component: "+container.toString());
        }
        final boolean[] rtn= { true };
        page.visitChildren((Class<?>)MarkupContainer.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<MarkupContainer,Void>(){
            public void component(final MarkupContainer comp,final IVisit<Void> visit){
                final IMarkupFragment associatedMarkup=comp.getAssociatedMarkup();
                if(associatedMarkup!=null){
                    final MarkupResourceStream rs=associatedMarkup.getMarkupResourceStream();
                    if(!rs.isHtml5()){
                        rtn[0]=false;
                        visit.stop();
                    }
                }
            }
        });
        return rtn[0];
    }
}
