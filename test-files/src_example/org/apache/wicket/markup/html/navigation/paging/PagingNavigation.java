package org.apache.wicket.markup.html.navigation.paging;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.basic.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.collections.*;
import java.util.*;

public class PagingNavigation extends Loop{
    private static final long serialVersionUID=1L;
    protected IPageable pageable;
    protected IPagingLabelProvider labelProvider;
    private int startIndex;
    private int margin;
    private String separator;
    private int viewSize;
    public PagingNavigation(final String id,final IPageable pageable){
        this(id,pageable,null);
    }
    public PagingNavigation(final String id,final IPageable pageable,final IPagingLabelProvider labelProvider){
        super(id,null);
        this.margin=-1;
        this.separator=null;
        this.viewSize=10;
        this.pageable=pageable;
        this.labelProvider=labelProvider;
        this.startIndex=0;
    }
    public int getMargin(){
        if(this.margin==-1&&this.viewSize!=0){
            return this.viewSize/2;
        }
        return this.margin;
    }
    public String getSeparator(){
        return this.separator;
    }
    public int getViewSize(){
        return this.viewSize;
    }
    public void setViewSize(final int size){
        this.viewSize=size;
    }
    public void setMargin(final int margin){
        this.margin=margin;
    }
    public void setSeparator(final String separator){
        this.separator=separator;
    }
    protected void onBeforeRender(){
        this.setDefaultModel(new Model<Object>((Object)this.pageable.getPageCount()));
        this.setStartIndex();
        super.onBeforeRender();
    }
    protected final int getStartIndex(){
        return this.startIndex;
    }
    protected void populateItem(final LoopItem loopItem){
        final int pageIndex=this.getStartIndex()+loopItem.getIndex();
        final AbstractLink link=this.newPagingNavigationLink("pageLink",this.pageable,pageIndex);
        link.add(new TitleAppender(pageIndex));
        loopItem.add(link);
        String label="";
        if(this.labelProvider!=null){
            label=this.labelProvider.getPageLabel(pageIndex);
        }
        else{
            label=String.valueOf(pageIndex+1);
        }
        link.add(new Label("pageNumber",label));
    }
    protected AbstractLink newPagingNavigationLink(final String id,final IPageable pageable,final int pageIndex){
        return new PagingNavigationLink<Object>(id,pageable,pageIndex);
    }
    protected void renderItem(final LoopItem loopItem){
        super.renderItem(loopItem);
        if(this.separator!=null&&loopItem.getIndex()!=this.getIterations()-1){
            this.getResponse().write((CharSequence)this.separator);
        }
    }
    private void setStartIndex(){
        int firstListItem=this.startIndex;
        final int viewSize=Math.min(this.getViewSize(),this.pageable.getPageCount());
        final int margin=this.getMargin();
        final int currentPage=this.pageable.getCurrentPage();
        if(currentPage<firstListItem+margin){
            firstListItem=currentPage-margin;
        }
        else if(currentPage>=firstListItem+viewSize-margin){
            firstListItem=currentPage+margin+1-viewSize;
        }
        if(firstListItem+viewSize>=this.pageable.getPageCount()){
            firstListItem=this.pageable.getPageCount()-viewSize;
        }
        if(firstListItem<0){
            firstListItem=0;
        }
        if(viewSize!=this.getIterations()||this.startIndex!=firstListItem){
            this.modelChanging();
            this.addStateChange();
            this.startIndex=firstListItem;
            this.setIterations(Math.min(viewSize,this.pageable.getPageCount()));
            this.modelChanged();
            this.removeAll();
        }
    }
    private void setIterations(final int i){
        this.setDefaultModelObject(i);
    }
    private final class TitleAppender extends Behavior{
        private static final long serialVersionUID=1L;
        private static final String RES="PagingNavigation.page";
        private final int page;
        public TitleAppender(final int page){
            super();
            this.page=page;
        }
        public void onComponentTag(final Component component,final ComponentTag tag){
            final Map<String,String> vars=(Map<String,String>)new MicroMap((Object)"page",(Object)String.valueOf(this.page+1));
            tag.put("title",(CharSequence)PagingNavigation.this.getString("PagingNavigation.page",Model.ofMap(vars)));
        }
    }
}
