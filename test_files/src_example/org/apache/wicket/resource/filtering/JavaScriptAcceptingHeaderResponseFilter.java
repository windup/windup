package org.apache.wicket.resource.filtering;

public class JavaScriptAcceptingHeaderResponseFilter extends OppositeHeaderResponseFilter{
    public JavaScriptAcceptingHeaderResponseFilter(final String name){
        super(name,new CssAcceptingHeaderResponseFilter("NOT_USED"));
    }
}
