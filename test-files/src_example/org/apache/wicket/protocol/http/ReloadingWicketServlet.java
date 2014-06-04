package org.apache.wicket.protocol.http;

public class ReloadingWicketServlet extends WicketServlet{
    private static final long serialVersionUID=1L;
    protected WicketFilter newWicketFilter(){
        return new ReloadingWicketFilter();
    }
}
