package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.serialize.*;
import org.apache.wicket.serialize.java.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.event.*;
import java.util.*;

public class FrameworkSettings implements IFrameworkSettings{
    private IDetachListener detachListener;
    private List<IEventDispatcher> eventDispatchers;
    private ISerializer pageSerializer;
    public FrameworkSettings(final Application application){
        super();
        this.eventDispatchers=null;
        this.pageSerializer=(ISerializer)new JavaSerializer(application.getApplicationKey());
    }
    public String getVersion(){
        String implVersion=null;
        final Package pkg=this.getClass().getPackage();
        if(pkg!=null){
            implVersion=pkg.getImplementationVersion();
        }
        return Strings.isEmpty((CharSequence)implVersion)?"n/a":implVersion;
    }
    public IDetachListener getDetachListener(){
        return this.detachListener;
    }
    public void setDetachListener(final IDetachListener detachListener){
        this.detachListener=detachListener;
    }
    public void add(final IEventDispatcher dispatcher){
        Args.notNull((Object)dispatcher,"dispatcher");
        if(this.eventDispatchers==null){
            this.eventDispatchers=(List<IEventDispatcher>)new ArrayList();
        }
        if(!this.eventDispatchers.contains(dispatcher)){
            this.eventDispatchers.add(dispatcher);
        }
    }
    public void dispatchEvent(final Object sink,final IEvent<?> event,final Component component){
        if(component!=null&&sink instanceof IComponentAwareEventSink){
            ((IComponentAwareEventSink)sink).onEvent(component,event);
        }
        else if(sink instanceof IEventSink){
            ((IEventSink)sink).onEvent(event);
        }
        if(this.eventDispatchers==null){
            return;
        }
        for(final IEventDispatcher dispatcher : this.eventDispatchers){
            dispatcher.dispatchEvent(sink,event,component);
        }
    }
    public void setSerializer(final ISerializer pageSerializer){
        this.pageSerializer=(ISerializer)Args.notNull((Object)pageSerializer,"pageSerializer");
    }
    public ISerializer getSerializer(){
        return this.pageSerializer;
    }
}
