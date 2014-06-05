package org.apache.wicket.request.handler;

public interface IIntrospectablePageProvider{
    boolean hasPageInstance();
    boolean isPageInstanceFresh();
}
