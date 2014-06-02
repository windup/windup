package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.behavior.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;
import org.slf4j.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.string.*;

public final class RelativePathPrefixHandler extends AbstractMarkupFilter implements IComponentResolver{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    public static final String WICKET_RELATIVE_PATH_PREFIX_CONTAINER_ID="_relative_path_prefix_";
    private static final String[] attributeNames;
    public static final Behavior RELATIVE_PATH_BEHAVIOR;
    public RelativePathPrefixHandler(){
        this(null);
    }
    public RelativePathPrefixHandler(final MarkupResourceStream markup){
        super(markup);
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag.isClose()){
            return tag;
        }
        final String wicketIdAttr=this.getWicketNamespace()+":"+"id";
        if(tag instanceof WicketTag||tag.isAutolinkEnabled()||tag.getAttributes().get((Object)wicketIdAttr)!=null){
            return tag;
        }
        for(final String attrName : RelativePathPrefixHandler.attributeNames){
            final String attrValue=tag.getAttributes().getString(attrName);
            if(attrValue!=null&&!attrValue.startsWith("/")&&!attrValue.contains((CharSequence)":")&&!attrValue.startsWith("#")){
                if(tag.getId()==null){
                    tag.setId("_relative_path_prefix_");
                    tag.setAutoComponentTag(true);
                }
                tag.addBehavior(RelativePathPrefixHandler.RELATIVE_PATH_BEHAVIOR);
                tag.setModified(true);
                break;
            }
        }
        return tag;
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag!=null&&tag.getId().startsWith("_relative_path_prefix_")){
            final String id="_relative_path_prefix_"+container.getPage().getAutoIndex();
            return new TransparentWebMarkupContainer(id);
        }
        return null;
    }
    static{
        log=LoggerFactory.getLogger(RelativePathPrefixHandler.class);
        attributeNames=new String[] { "href","src","background","action" };
        RELATIVE_PATH_BEHAVIOR=new Behavior(){
            private static final long serialVersionUID=1L;
            public void onComponentTag(final Component component,final ComponentTag tag){
                for(final String attrName : RelativePathPrefixHandler.attributeNames){
                    final String attrValue=tag.getAttributes().getString(attrName);
                    if(attrValue!=null&&!attrValue.startsWith("/")&&!attrValue.contains((CharSequence)":")&&!attrValue.startsWith("#")){
                        tag.getAttributes().put((Object)attrName,(Object)UrlUtils.rewriteToContextRelative(attrValue,RequestCycle.get()));
                    }
                }
            }
        };
    }
}
