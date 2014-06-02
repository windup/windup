package org.apache.wicket.event;

public interface IEventSource{
     <T> void send(IEventSink p0,Broadcast p1,T p2);
}
