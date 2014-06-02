package org.apache.wicket;

import org.apache.wicket.event.*;

final class ComponentEvent<T> implements IEvent<T>{
    private final IEventSink sink;
    private final IEventSource source;
    private final Broadcast type;
    private final T payload;
    private boolean stop;
    private boolean shallow;
    public ComponentEvent(final IEventSink sink,final IEventSource source,final Broadcast broadcast,final T payload){
        super();
        this.sink=sink;
        this.source=source;
        this.type=broadcast;
        this.payload=payload;
    }
    public IEventSink getSink(){
        return this.sink;
    }
    public IEventSource getSource(){
        return this.source;
    }
    public Broadcast getType(){
        return this.type;
    }
    public T getPayload(){
        return this.payload;
    }
    public void dontBroadcastDeeper(){
        this.shallow=true;
    }
    public void stop(){
        this.stop=true;
    }
    boolean isStop(){
        return this.stop;
    }
    boolean isShallow(){
        return this.shallow;
    }
    void resetShallow(){
        this.shallow=false;
    }
}
