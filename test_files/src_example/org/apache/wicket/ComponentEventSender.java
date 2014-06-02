package org.apache.wicket;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.event.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.behavior.*;
import java.util.*;
import org.apache.wicket.util.visit.*;

final class ComponentEventSender implements IEventSource{
    private final Component source;
    private final IEventDispatcher dispatcher;
    public ComponentEventSender(final Component source,final IEventDispatcher dispatcher){
        super();
        Args.notNull((Object)source,"source");
        Args.notNull((Object)dispatcher,"dispatcher");
        this.source=source;
        this.dispatcher=dispatcher;
    }
    public <T> void send(final IEventSink sink,final Broadcast type,final T payload){
        final ComponentEvent<?> event=new ComponentEvent<Object>(sink,this.source,type,payload);
        Args.notNull((Object)type,"type");
        switch(type){
            case BUBBLE:{
                this.bubble(event);
                break;
            }
            case BREADTH:{
                this.breadth(event);
                break;
            }
            case DEPTH:{
                this.depth(event);
                break;
            }
            case EXACT:{
                this.dispatcher.dispatchEvent(event.getSink(),event,(sink instanceof Component)?((Component)sink):null);
                break;
            }
        }
    }
    private void breadth(final ComponentEvent<?> event){
        final IEventSink sink=event.getSink();
        final boolean targetsApplication=sink instanceof Application;
        final boolean targetsSession=targetsApplication||sink instanceof Session;
        final boolean targetsCycle=targetsSession||sink instanceof RequestCycle;
        final boolean targetsComponent=sink instanceof Component;
        if(!targetsComponent&&!targetsCycle){
            this.dispatcher.dispatchEvent(sink,event,null);
            return;
        }
        if(targetsApplication){
            this.dispatcher.dispatchEvent(this.source.getApplication(),event,null);
        }
        if(event.isStop()){
            return;
        }
        if(targetsSession){
            this.dispatcher.dispatchEvent(this.source.getSession(),event,null);
        }
        if(event.isStop()){
            return;
        }
        if(targetsCycle){
            this.dispatcher.dispatchEvent(this.source.getRequestCycle(),event,null);
        }
        if(event.isStop()){
            return;
        }
        final Component cursor=targetsCycle?this.source.getPage():((Component)sink);
        dispatchToComponent(this.dispatcher,cursor,event);
        if(event.isStop()){
            return;
        }
        event.resetShallow();
        if(cursor instanceof MarkupContainer){
            ((MarkupContainer)cursor).visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new ComponentEventVisitor((ComponentEvent)event,this.dispatcher));
        }
    }
    private void depth(final ComponentEvent<?> event){
        final IEventSink sink=event.getSink();
        final boolean targetsApplication=sink instanceof Application;
        final boolean targetsSession=targetsApplication||sink instanceof Session;
        final boolean targetsCycle=targetsSession||sink instanceof RequestCycle;
        final boolean targetsComponent=sink instanceof Component;
        if(!targetsComponent&&!targetsCycle){
            this.dispatcher.dispatchEvent(sink,event,null);
            return;
        }
        final Component cursor=targetsCycle?this.source.getPage():((Component)sink);
        if(cursor instanceof MarkupContainer){
            Visits.visitPostOrder((Object)cursor,(IVisitor)new ComponentEventVisitor((ComponentEvent)event,this.dispatcher));
        }
        else{
            dispatchToComponent(this.dispatcher,cursor,event);
        }
        if(event.isStop()){
            return;
        }
        if(targetsCycle){
            this.dispatcher.dispatchEvent(this.source.getRequestCycle(),event,null);
        }
        if(event.isStop()){
            return;
        }
        if(targetsSession){
            this.dispatcher.dispatchEvent(this.source.getSession(),event,null);
        }
        if(event.isStop()){
            return;
        }
        if(targetsApplication){
            this.dispatcher.dispatchEvent(this.source.getApplication(),event,null);
        }
    }
    private void bubble(final ComponentEvent<?> event){
        final IEventSink sink=event.getSink();
        final boolean targetsComponent=sink instanceof Component;
        final boolean targetsCycle=targetsComponent||sink instanceof RequestCycle;
        final boolean targetsSession=targetsCycle||sink instanceof Session;
        final boolean targetsApplication=targetsSession||sink instanceof Application;
        if(!targetsApplication&&!targetsComponent){
            this.dispatcher.dispatchEvent(sink,event,null);
            return;
        }
        if(targetsComponent){
            final Component cursor=(Component)sink;
            dispatchToComponent(this.dispatcher,cursor,event);
            if(event.isStop()){
                return;
            }
            cursor.visitParents((Class<?>)Component.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new ComponentEventVisitor((ComponentEvent)event,this.dispatcher));
        }
        if(event.isStop()){
            return;
        }
        if(targetsCycle){
            this.dispatcher.dispatchEvent(this.source.getRequestCycle(),event,null);
        }
        if(event.isStop()){
            return;
        }
        if(targetsSession){
            this.dispatcher.dispatchEvent(this.source.getSession(),event,null);
        }
        if(event.isStop()){
            return;
        }
        if(targetsApplication){
            this.dispatcher.dispatchEvent(this.source.getApplication(),event,null);
        }
    }
    private static void dispatchToComponent(final IEventDispatcher dispatcher,final Component object,final ComponentEvent<?> e){
        dispatcher.dispatchEvent(object,e,null);
        if(e.isStop()){
            return;
        }
        final List<? extends Behavior> behaviors=object.getBehaviors();
        for(final Behavior behavior : behaviors){
            dispatcher.dispatchEvent(behavior,e,object);
            if(e.isStop()){
                break;
            }
        }
    }
    private static class ComponentEventVisitor implements IVisitor<Component,Void>{
        private final ComponentEvent<?> e;
        private final IEventDispatcher dispatcher;
        private ComponentEventVisitor(final ComponentEvent<?> event,final IEventDispatcher dispatcher){
            super();
            this.e=event;
            this.dispatcher=dispatcher;
        }
        public void component(final Component object,final IVisit<Void> visit){
            dispatchToComponent(this.dispatcher,object,this.e);
            if(this.e.isStop()){
                visit.stop();
            }
            if(this.e.isShallow()){
                visit.dontGoDeeper();
            }
            this.e.resetShallow();
        }
    }
}
