package org.apache.wicket.feedback;

import org.apache.wicket.*;

public interface IFeedbackMessageFilter extends IClusterable{
    public static final IFeedbackMessageFilter ALL=new IFeedbackMessageFilter(){
        private static final long serialVersionUID=1L;
        public boolean accept(FeedbackMessage message){
            return true;
        }
    };
    boolean accept(FeedbackMessage p0);
}
