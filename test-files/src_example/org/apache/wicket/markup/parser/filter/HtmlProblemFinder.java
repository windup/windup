package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import java.util.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public final class HtmlProblemFinder extends AbstractMarkupFilter{
    private static final Logger log;
    public static final int ERR_INGORE=3;
    public static final int ERR_LOG_WARN=2;
    public static final int ERR_LOG_ERROR=1;
    public static final int ERR_THROW_EXCEPTION=0;
    private final int problemEscalation;
    public HtmlProblemFinder(final int problemEscalation){
        super();
        this.problemEscalation=problemEscalation;
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if("img".equals(tag.getName())&&(tag.isOpen()||tag.isOpenClose())){
            final String src=tag.getAttributes().getString("src");
            if(src!=null&&src.trim().length()==0){
                this.escalateWarning("Attribute 'src' should not be empty. Location: ",tag);
            }
        }
        for(String key : tag.getAttributes().keySet()){
            if(key!=null){
                key=key.toLowerCase();
                if(!key.startsWith("wicket.")){
                    continue;
                }
                this.escalateWarning("You probably want 'wicket:xxx' rather than 'wicket.xxx'. Location: ",tag);
            }
        }
        return tag;
    }
    private void escalateWarning(final String msg,final ComponentTag tag) throws WicketParseException{
        if(this.problemEscalation==2){
            HtmlProblemFinder.log.warn(msg+tag.toUserDebugString());
        }
        else if(this.problemEscalation==1){
            HtmlProblemFinder.log.error(msg+tag.toUserDebugString());
        }
        else if(this.problemEscalation!=3){
            throw new WicketParseException(msg,tag);
        }
    }
    static{
        log=LoggerFactory.getLogger(HtmlProblemFinder.class);
    }
}
