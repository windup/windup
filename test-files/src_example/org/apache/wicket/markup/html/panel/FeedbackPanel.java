package org.apache.wicket.markup.html.panel;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.feedback.*;
import java.util.*;
import org.apache.wicket.markup.html.basic.*;
import java.io.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.*;
import org.apache.wicket.behavior.*;

public class FeedbackPanel extends Panel implements IFeedback{
    private static final long serialVersionUID=1L;
    private final MessageListView messageListView;
    public FeedbackPanel(final String id){
        this(id,(IFeedbackMessageFilter)null);
    }
    public FeedbackPanel(final String id,final IFeedbackMessageFilter filter){
        super(id);
        final WebMarkupContainer messagesContainer=new WebMarkupContainer("feedbackul"){
            private static final long serialVersionUID=1L;
            protected void onConfigure(){
                super.onConfigure();
                this.setVisible(FeedbackPanel.this.anyMessage());
            }
        };
        this.add(messagesContainer);
        (this.messageListView=new MessageListView("messages")).setVersioned(false);
        messagesContainer.add(this.messageListView);
        if(filter!=null){
            this.setFilter(filter);
        }
    }
    public final boolean anyErrorMessage(){
        return this.anyMessage(400);
    }
    public final boolean anyMessage(){
        return this.anyMessage(0);
    }
    public final boolean anyMessage(final int level){
        final List<FeedbackMessage> msgs=this.getCurrentMessages();
        for(final FeedbackMessage msg : msgs){
            if(msg.isLevel(level)){
                return true;
            }
        }
        return false;
    }
    public final FeedbackMessagesModel getFeedbackMessagesModel(){
        return (FeedbackMessagesModel)this.messageListView.getDefaultModel();
    }
    public final IFeedbackMessageFilter getFilter(){
        return this.getFeedbackMessagesModel().getFilter();
    }
    public final Comparator<FeedbackMessage> getSortingComparator(){
        return this.getFeedbackMessagesModel().getSortingComparator();
    }
    public boolean isVersioned(){
        return false;
    }
    public final FeedbackPanel setFilter(final IFeedbackMessageFilter filter){
        this.getFeedbackMessagesModel().setFilter(filter);
        return this;
    }
    public final FeedbackPanel setMaxMessages(final int maxMessages){
        this.messageListView.setViewSize(maxMessages);
        return this;
    }
    public final FeedbackPanel setSortingComparator(final Comparator<FeedbackMessage> sortingComparator){
        this.getFeedbackMessagesModel().setSortingComparator(sortingComparator);
        return this;
    }
    protected String getCSSClass(final FeedbackMessage message){
        return "feedbackPanel"+message.getLevelAsString();
    }
    protected final List<FeedbackMessage> getCurrentMessages(){
        final List<FeedbackMessage> messages=this.messageListView.getModelObject();
        return (List<FeedbackMessage>)Collections.unmodifiableList(messages);
    }
    protected FeedbackMessagesModel newFeedbackMessagesModel(){
        return new FeedbackMessagesModel(this);
    }
    protected Component newMessageDisplayComponent(final String id,final FeedbackMessage message){
        final Serializable serializable=message.getMessage();
        final Label label=new Label(id,(serializable==null)?"":serializable.toString());
        label.setEscapeModelStrings(this.getEscapeModelStrings());
        return label;
    }
    private final class MessageListView extends ListView<FeedbackMessage>{
        private static final long serialVersionUID=1L;
        public MessageListView(final String id){
            super(id);
            this.setDefaultModel(FeedbackPanel.this.newFeedbackMessagesModel());
        }
        protected IModel<FeedbackMessage> getListItemModel(final IModel<? extends List<FeedbackMessage>> listViewModel,final int index){
            return new AbstractReadOnlyModel<FeedbackMessage>(){
                private static final long serialVersionUID=1L;
                public FeedbackMessage getObject(){
                    if(index>=listViewModel.getObject().size()){
                        return null;
                    }
                    return (FeedbackMessage)listViewModel.getObject().get(index);
                }
            };
        }
        protected void populateItem(final ListItem<FeedbackMessage> listItem){
            final FeedbackMessage message=listItem.getModelObject();
            message.markRendered();
            final Component label=FeedbackPanel.this.newMessageDisplayComponent("message",message);
            final AttributeModifier levelModifier=AttributeModifier.append("class",FeedbackPanel.this.getCSSClass(message));
            label.add(levelModifier);
            listItem.add(levelModifier);
            listItem.add(label);
        }
    }
}
