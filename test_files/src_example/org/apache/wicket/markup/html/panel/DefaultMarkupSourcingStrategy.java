package org.apache.wicket.markup.html.panel;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.markup.html.internal.*;
import org.slf4j.*;

public final class DefaultMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy{
    private static final Logger log;
    private static DefaultMarkupSourcingStrategy instance;
    public static final DefaultMarkupSourcingStrategy get(){
        return DefaultMarkupSourcingStrategy.instance;
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
    }
    public void onComponentTagBody(final Component component,final MarkupStream markupStream,final ComponentTag openTag){
        component.onComponentTagBody(markupStream,openTag);
    }
    public IMarkupFragment getMarkup(final MarkupContainer container,final Component child){
        IMarkupFragment markup=container.getMarkup();
        if(markup==null){
            return null;
        }
        if(child==null){
            return markup;
        }
        markup=markup.find(child.getId());
        if(markup!=null){
            return markup;
        }
        markup=this.searchMarkupInTransparentResolvers(container,child);
        if(markup!=null){
            return markup;
        }
        if(Character.isDigit(child.getId().charAt(0))){
            final String id=child.getId();
            boolean miss=false;
            for(int i=1;i<id.length();++i){
                if(!Character.isDigit(id.charAt(i))){
                    miss=true;
                    break;
                }
            }
            if(!miss){
                markup=container.getMarkup();
                if(!(child instanceof AbstractItem)&&DefaultMarkupSourcingStrategy.log.isWarnEnabled()){
                    DefaultMarkupSourcingStrategy.log.warn("1.4 to 1.5 migration issue: the childs wicket-id contains decimals only. By convention that is only the case for children (Items) of Loop, ListView, Tree etc.. To avoid the warning, the childs container should implement:\n@Override public IMarkupFragment getMarkup(Component child) {\n// The childs markup is always equal to the parents markup.\nreturn getMarkup(); }\nChild: "+child.toString()+"\nContainer: "+container.toString());
                }
            }
        }
        return markup;
    }
    public void renderHead(final Component component,final HtmlHeaderContainer container){
    }
    static{
        log=LoggerFactory.getLogger(DefaultMarkupSourcingStrategy.class);
        DefaultMarkupSourcingStrategy.instance=new DefaultMarkupSourcingStrategy();
    }
}
