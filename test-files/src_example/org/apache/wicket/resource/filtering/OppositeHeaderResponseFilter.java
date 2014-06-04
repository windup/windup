package org.apache.wicket.resource.filtering;

import org.apache.wicket.request.resource.*;

public class OppositeHeaderResponseFilter implements HeaderResponseContainerFilteringHeaderResponse.IHeaderResponseFilter{
    private final String name;
    private final HeaderResponseContainerFilteringHeaderResponse.IHeaderResponseFilter other;
    public OppositeHeaderResponseFilter(final String name,final HeaderResponseContainerFilteringHeaderResponse.IHeaderResponseFilter other){
        super();
        this.name=name;
        this.other=other;
    }
    public String getName(){
        return this.name;
    }
    public boolean acceptReference(final ResourceReference ref){
        return !this.other.acceptReference(ref);
    }
    public boolean acceptOtherJavaScript(){
        return !this.other.acceptOtherJavaScript();
    }
    public boolean acceptOtherCss(){
        return !this.other.acceptOtherCss();
    }
}
