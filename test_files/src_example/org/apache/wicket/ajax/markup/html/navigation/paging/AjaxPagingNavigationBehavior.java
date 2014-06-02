package org.apache.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.ajax.markup.html.*;
import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.ajax.calldecorator.*;
import org.apache.wicket.markup.*;

public class AjaxPagingNavigationBehavior extends AjaxEventBehavior{
    private static final long serialVersionUID=1L;
    private final IAjaxLink owner;
    public AjaxPagingNavigationBehavior(final IAjaxLink owner,final IPageable pageable,final String event){
        super(event);
        this.owner=owner;
    }
    protected void onEvent(final AjaxRequestTarget target){
        this.owner.onClick(target);
        final AjaxPagingNavigator navigator=((Component)this.owner).findParent((Class<AjaxPagingNavigator>)AjaxPagingNavigator.class);
        if(navigator!=null){
            navigator.onAjaxEvent(target);
        }
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return new CancelEventIfNoAjaxDecorator();
    }
    protected void onComponentTag(final ComponentTag tag){
        if(this.getComponent().isEnabledInHierarchy()){
            super.onComponentTag(tag);
        }
    }
}
