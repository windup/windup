package org.apache.wicket.markup.renderStrategy;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.util.visit.*;
import java.util.*;

public abstract class DeepChildFirstVisitor implements IVisitor<Component,Void>{
    public final Visit<Void> visit(final Component rootComponent){
        final Visit<Void> visitor=(Visit<Void>)new Visit();
        return this.visit(rootComponent,visitor);
    }
    public final Visit<Void> visit(final Component rootComponent,final Visit<Void> visit){
        Args.notNull((Object)rootComponent,"rootComponent");
        Args.notNull((Object)visit,"visit");
        if(!(rootComponent instanceof MarkupContainer)){
            this.component(rootComponent,(IVisit<Void>)visit);
            return visit;
        }
        if(!this.preCheck(rootComponent)){
            return visit;
        }
        if(visit.isContinue()){
            for(final Component child : (MarkupContainer)rootComponent){
                this.visit(child,visit);
                if(visit.isStopped()){
                    return visit;
                }
            }
        }
        this.component(rootComponent,(IVisit<Void>)visit);
        return visit;
    }
    public abstract void component(final Component p0,final IVisit<Void> p1);
    public abstract boolean preCheck(final Component p0);
}
