package org.apache.wicket.feedback;

public class ErrorLevelFeedbackMessageFilter implements IFeedbackMessageFilter{
    private static final long serialVersionUID=1L;
    private final int minimumErrorLevel;
    public ErrorLevelFeedbackMessageFilter(final int minimumErrorLevel){
        super();
        this.minimumErrorLevel=minimumErrorLevel;
    }
    public boolean accept(final FeedbackMessage message){
        return message.isLevel(this.minimumErrorLevel);
    }
}
