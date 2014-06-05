package org.apache.wicket.protocol.http;

public interface IWebApplicationFactory{
    WebApplication createApplication(WicketFilter p0);
    void destroy(WicketFilter p0);
}
