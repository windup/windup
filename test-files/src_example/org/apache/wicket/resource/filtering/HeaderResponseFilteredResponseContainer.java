package org.apache.wicket.resource.filtering;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;

public class HeaderResponseFilteredResponseContainer extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private final String filterName;
    public HeaderResponseFilteredResponseContainer(final String id,final String filterName){
        super(id);
        this.filterName=filterName;
        this.setRenderBodyOnly(true);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.setType(XmlTag.TagType.OPEN);
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final HeaderResponseContainerFilteringHeaderResponse response=HeaderResponseContainerFilteringHeaderResponse.get();
        if(!response.isClosed()){
            throw new RuntimeException("there was an error processing the header response - you tried to render a bucket of response from HeaderResponseContainerFilteringHeaderResponse, but it had not yet run and been closed.  this should occur when the header container that is standard in wicket renders, so perhaps you have done something to keep that from rendering?");
        }
        final CharSequence foot=response.getContent(this.filterName);
        this.replaceComponentTagBody(markupStream,openTag,foot);
    }
}
