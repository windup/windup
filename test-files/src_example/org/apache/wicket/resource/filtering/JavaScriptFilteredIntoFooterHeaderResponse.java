package org.apache.wicket.resource.filtering;

import org.apache.wicket.markup.html.*;

public final class JavaScriptFilteredIntoFooterHeaderResponse extends HeaderResponseContainerFilteringHeaderResponse{
    private static final String HEADER_FILTER_NAME="headerBucket";
    public JavaScriptFilteredIntoFooterHeaderResponse(final IHeaderResponse response,final String footerBucketName){
        super(response,"headerBucket",null);
        this.setFilters(this.createFilters(footerBucketName));
    }
    private IHeaderResponseFilter[] createFilters(final String footerBucketName){
        final IHeaderResponseFilter footer=this.createFooterFilter(footerBucketName);
        final IHeaderResponseFilter header=this.createHeaderFilter("headerBucket",footer);
        return new IHeaderResponseFilter[] { header,footer };
    }
    private IHeaderResponseFilter createFooterFilter(final String footerBucketName){
        return new JavaScriptAcceptingHeaderResponseFilter(footerBucketName);
    }
    private IHeaderResponseFilter createHeaderFilter(final String headerFilterName,final IHeaderResponseFilter footerFilter){
        return new OppositeHeaderResponseFilter(headerFilterName,footerFilter);
    }
}
