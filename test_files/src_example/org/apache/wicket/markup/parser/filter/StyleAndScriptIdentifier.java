package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import java.util.regex.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.lang.*;

public final class StyleAndScriptIdentifier extends AbstractMarkupFilter{
    private static final Pattern HTML_START_COMMENT;
    private static final Pattern CDATA_START_COMMENT;
    private static final Pattern JS_CDATA_START_COMMENT;
    public StyleAndScriptIdentifier(final Markup markup){
        super();
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag.getNamespace()!=null){
            return tag;
        }
        final String tagName=tag.getName();
        final boolean isScript="script".equalsIgnoreCase(tagName);
        final boolean isStyle="style".equalsIgnoreCase(tagName);
        if(isScript||isStyle){
            if(tag.isOpen()&&tag.getId()==null&&((isScript&&tag.getAttribute("src")==null)||isStyle)){
                tag.setId("_ScriptStyle");
                tag.setModified(true);
            }
            tag.setFlag(32,true);
            tag.setUserData("STYLE_OR_SCRIPT",Boolean.TRUE);
        }
        return tag;
    }
    public void postProcess(final Markup markup){
        for(int i=0;i<markup.size();++i){
            final MarkupElement elem=markup.get(i);
            if(elem instanceof ComponentTag){
                final ComponentTag open=(ComponentTag)elem;
                if(this.shouldProcess(open)&&open.isOpen()&&i+2<markup.size()){
                    final MarkupElement body=markup.get(i+1);
                    final MarkupElement tag2=markup.get(i+2);
                    if(body instanceof RawMarkup&&tag2 instanceof ComponentTag){
                        final ComponentTag close=(ComponentTag)tag2;
                        if(close.closes(open)){
                            String text=body.toString().trim();
                            if(this.shouldWrapInCdata(text)){
                                text="\n/*<![CDATA[*/\n"+body.toString()+"\n/*]]>*/\n";
                                markup.replace(i+1,new RawMarkup((CharSequence)text));
                            }
                        }
                    }
                }
            }
        }
    }
    boolean shouldWrapInCdata(final String elementBody){
        Args.notNull((Object)elementBody,"elementBody");
        boolean shouldWrap=true;
        if(StyleAndScriptIdentifier.HTML_START_COMMENT.matcher((CharSequence)elementBody).matches()||StyleAndScriptIdentifier.CDATA_START_COMMENT.matcher((CharSequence)elementBody).matches()||StyleAndScriptIdentifier.JS_CDATA_START_COMMENT.matcher((CharSequence)elementBody).matches()){
            shouldWrap=false;
        }
        return shouldWrap;
    }
    private boolean shouldProcess(final ComponentTag openTag){
        final String typeAttribute=openTag.getAttribute("type");
        final boolean shouldProcess="style".equals(openTag.getName())||typeAttribute==null||"text/javascript".equalsIgnoreCase(typeAttribute);
        return shouldProcess&&openTag.getUserData("STYLE_OR_SCRIPT")!=null;
    }
    static{
        HTML_START_COMMENT=Pattern.compile("^\\s*<!--\\s*.*",32);
        CDATA_START_COMMENT=Pattern.compile("^\\s*<!\\[CDATA\\[\\s*.*",32);
        JS_CDATA_START_COMMENT=Pattern.compile("^\\s*\\/\\*\\s*<!\\[CDATA\\[\\s*\\*\\/\\s*.*",32);
    }
}
