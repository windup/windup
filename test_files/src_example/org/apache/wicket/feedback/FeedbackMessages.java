package org.apache.wicket.feedback;

import java.util.concurrent.*;
import org.apache.wicket.*;
import java.io.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public final class FeedbackMessages implements IClusterable,Iterable<FeedbackMessage>{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    private final List<FeedbackMessage> messages;
    public FeedbackMessages(){
        super();
        this.messages=new CopyOnWriteArrayList<FeedbackMessage>();
    }
    public final void add(final FeedbackMessage message){
        FeedbackMessages.log.debug("Adding feedback message '{}'",message);
        synchronized(this.messages){
            this.messages.add(message);
        }
    }
    public final void add(final Component reporter,final Serializable message,final int level){
        this.add(new FeedbackMessage(reporter,message,level));
    }
    public final void debug(final Component reporter,final Serializable message){
        this.add(new FeedbackMessage(reporter,message,100));
    }
    public final void info(final Component reporter,final Serializable message){
        this.add(new FeedbackMessage(reporter,message,200));
    }
    public final void success(final Component reporter,final Serializable message){
        this.add(new FeedbackMessage(reporter,message,250));
    }
    public final void warn(final Component reporter,final Serializable message){
        this.add(new FeedbackMessage(reporter,message,300));
    }
    public final void error(final Component reporter,final Serializable message){
        this.add(new FeedbackMessage(reporter,message,400));
    }
    public final void fatal(final Component reporter,final Serializable message){
        this.add(new FeedbackMessage(reporter,message,500));
    }
    public final int clear(){
        return this.clear(null);
    }
    public final int clear(final IFeedbackMessageFilter filter){
        if(this.messages.isEmpty()){
            return 0;
        }
        final List<FeedbackMessage> toDelete=this.messages(filter);
        for(final FeedbackMessage message : toDelete){
            message.detach();
        }
        synchronized(this.messages){
            final int sizeBefore=this.messages.size();
            this.messages.removeAll(toDelete);
            final int sizeAfter=this.messages.size();
            return sizeAfter-sizeBefore;
        }
    }
    public final boolean hasMessage(final IFeedbackMessageFilter filter){
        for(final FeedbackMessage message : this.messages){
            if(filter==null||filter.accept(message)){
                return true;
            }
        }
        return false;
    }
    public final boolean hasMessageFor(final Component component){
        return this.hasMessage(new ComponentFeedbackMessageFilter(component));
    }
    public final boolean hasMessageFor(final Component component,final int level){
        return this.hasMessage(new IFeedbackMessageFilter(){
            public boolean accept(final FeedbackMessage message){
                return Objects.equal((Object)message.getReporter(),(Object)component)&&message.isLevel(level);
            }
        });
    }
    public final boolean hasErrorMessageFor(final Component component){
        return this.hasMessageFor(component,400);
    }
    public final Iterator<FeedbackMessage> iterator(){
        return (Iterator<FeedbackMessage>)this.messages.iterator();
    }
    public final List<FeedbackMessage> messages(final IFeedbackMessageFilter filter){
        if(this.messages.isEmpty()){
            return (List<FeedbackMessage>)Collections.emptyList();
        }
        final List<FeedbackMessage> list=(List<FeedbackMessage>)new ArrayList();
        for(final FeedbackMessage message : this.messages){
            if(filter==null||filter.accept(message)){
                list.add(message);
            }
        }
        return list;
    }
    public final FeedbackMessage messageForComponent(final Component component){
        final List<FeedbackMessage> list=this.messagesForComponent(component);
        return list.isEmpty()?null:((FeedbackMessage)list.get(0));
    }
    public final List<FeedbackMessage> messagesForComponent(final Component component){
        return this.messages(new ComponentFeedbackMessageFilter(component));
    }
    public final boolean isEmpty(){
        return this.messages.isEmpty();
    }
    public final int size(){
        return this.messages.size();
    }
    public final int size(final IFeedbackMessageFilter filter){
        int count=0;
        for(final FeedbackMessage message : this.messages){
            if(filter==null||filter.accept(message)){
                ++count;
            }
        }
        return count;
    }
    public String toString(){
        return "[feedbackMessages = "+StringList.valueOf((Collection)this.messages)+']';
    }
    static{
        log=LoggerFactory.getLogger(FeedbackMessages.class);
    }
}
