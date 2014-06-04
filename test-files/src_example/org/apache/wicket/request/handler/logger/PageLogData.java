package org.apache.wicket.request.handler.logger;

import org.apache.wicket.request.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.*;

public class PageLogData implements ILogData{
    private static final long serialVersionUID=1L;
    private final Class<? extends IRequestablePage> pageClass;
    private final Integer pageId;
    private final PageParameters pageParameters;
    private final Integer renderCount;
    public PageLogData(final IPageProvider pageProvider){
        super();
        this.pageClass=tryToGetPageClass(pageProvider);
        this.pageId=pageProvider.getPageId();
        this.pageParameters=pageProvider.getPageParameters();
        this.renderCount=pageProvider.getRenderCount();
    }
    private static Class<? extends IRequestablePage> tryToGetPageClass(final IPageProvider pageProvider){
        try{
            return pageProvider.getPageClass();
        }
        catch(Exception e){
            return null;
        }
    }
    public PageLogData(final Page page){
        super();
        this.pageClass=page.getPageClass();
        this.pageId=page.getPageId();
        this.pageParameters=page.getPageParameters();
        this.renderCount=page.getRenderCount();
    }
    public final Class<? extends IRequestablePage> getPageClass(){
        return this.pageClass;
    }
    public final Integer getPageId(){
        return this.pageId;
    }
    public final PageParameters getPageParameters(){
        return this.pageParameters;
    }
    public final Integer getRenderCount(){
        return this.renderCount;
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("{");
        if(this.pageClass!=null){
            sb.append("pageClass=");
            sb.append(this.getPageClass().getName());
            sb.append(',');
        }
        sb.append("pageId=");
        sb.append(this.getPageId());
        sb.append(",pageParameters={");
        sb.append(this.getPageParameters());
        sb.append("},renderCount=");
        sb.append(this.getRenderCount());
        sb.append("}");
        return sb.toString();
    }
}
