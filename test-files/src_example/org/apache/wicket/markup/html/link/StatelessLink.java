package org.apache.wicket.markup.html.link;

public abstract class StatelessLink<T> extends Link<T>{
    private static final long serialVersionUID=1L;
    public StatelessLink(final String id){
        super(id);
    }
    protected boolean getStatelessHint(){
        return true;
    }
    protected CharSequence getURL(){
        return this.urlFor(ILinkListener.INTERFACE,this.getPage().getPageParameters());
    }
}
