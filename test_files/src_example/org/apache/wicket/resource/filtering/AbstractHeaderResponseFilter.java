package org.apache.wicket.resource.filtering;

import org.apache.wicket.request.resource.*;

public abstract class AbstractHeaderResponseFilter implements HeaderResponseContainerFilteringHeaderResponse.IHeaderResponseFilter{
    private final String name;
    public AbstractHeaderResponseFilter(final String name){
        super();
        this.name=name;
    }
    public String getName(){
        return this.name;
    }
    public boolean acceptReference(final ResourceReference object){
        return true;
    }
    public boolean acceptOtherJavaScript(){
        return true;
    }
    public boolean acceptOtherCss(){
        return true;
    }
}
