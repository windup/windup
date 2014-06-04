package org.apache.wicket;

public class PageReference implements IClusterable{
    private static final long serialVersionUID=1L;
    private final int pageId;
    public PageReference(final int pageId){
        super();
        this.pageId=pageId;
    }
    public Page getPage(){
        return (Page)Session.get().getPageManager().getPage(this.pageId);
    }
    public int getPageId(){
        return this.pageId;
    }
    public int hashCode(){
        return this.pageId;
    }
    public boolean equals(final Object obj){
        if(this==obj){
            return true;
        }
        if(obj==null){
            return false;
        }
        if(this.getClass()!=obj.getClass()){
            return false;
        }
        final PageReference other=(PageReference)obj;
        return this.getPageId()==other.getPageId();
    }
}
