package org.apache.wicket.feedback;

import org.apache.wicket.model.*;
import org.apache.wicket.*;
import java.util.*;
import java.io.*;

public class FeedbackMessagesModel implements IModel<List<FeedbackMessage>>{
    private static final long serialVersionUID=1L;
    private IFeedbackMessageFilter filter;
    private transient List<FeedbackMessage> messages;
    private Comparator<FeedbackMessage> sortingComparator;
    public FeedbackMessagesModel(final Component component){
        super();
        if(component==null){
            throw new IllegalArgumentException("Argument 'component' cannot be null");
        }
    }
    public FeedbackMessagesModel(final Page page,final IFeedbackMessageFilter filter){
        this(page);
        this.setFilter(filter);
    }
    public final IFeedbackMessageFilter getFilter(){
        return this.filter;
    }
    public final Comparator<FeedbackMessage> getSortingComparator(){
        return this.sortingComparator;
    }
    public final List<FeedbackMessage> getObject(){
        if(this.messages==null){
            this.messages=Session.get().getFeedbackMessages().messages(this.filter);
            if(this.sortingComparator!=null){
                Collections.sort(this.messages,this.sortingComparator);
            }
            this.messages=this.processMessages(this.messages);
        }
        return this.messages;
    }
    public final FeedbackMessagesModel setFilter(final IFeedbackMessageFilter filter){
        this.filter=filter;
        return this;
    }
    public final FeedbackMessagesModel setSortingComparator(final Comparator<FeedbackMessage> sortingComparator){
        if(!(sortingComparator instanceof Serializable)){
            throw new IllegalArgumentException("sortingComparator must be serializable");
        }
        this.sortingComparator=sortingComparator;
        return this;
    }
    protected List<FeedbackMessage> processMessages(final List<FeedbackMessage> messages){
        return messages;
    }
    public void setObject(final List<FeedbackMessage> object){
    }
    public void detach(){
        this.messages=null;
    }
}
