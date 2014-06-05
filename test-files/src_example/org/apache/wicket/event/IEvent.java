package org.apache.wicket.event;

public interface IEvent<T>{
    void stop();
    void dontBroadcastDeeper();
    Broadcast getType();
    IEventSource getSource();
    T getPayload();
}
