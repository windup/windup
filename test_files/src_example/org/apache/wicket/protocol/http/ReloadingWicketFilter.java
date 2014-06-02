package org.apache.wicket.protocol.http;

import org.apache.wicket.application.*;
import org.apache.wicket.util.listener.*;
import javax.servlet.*;

public class ReloadingWicketFilter extends WicketFilter{
    private ReloadingClassLoader reloadingClassLoader;
    public ReloadingWicketFilter(){
        super();
        this.reloadingClassLoader=new ReloadingClassLoader(this.getClass().getClassLoader());
    }
    protected ClassLoader getClassLoader(){
        return this.reloadingClassLoader;
    }
    public void init(final boolean isServlet,final FilterConfig filterConfig) throws ServletException{
        this.reloadingClassLoader.setListener((IChangeListener)new IChangeListener(){
            public void onChange(){
                ReloadingWicketFilter.this.destroy();
                ReloadingWicketFilter.this.reloadingClassLoader.destroy();
                ReloadingWicketFilter.this.reloadingClassLoader=new ReloadingClassLoader(this.getClass().getClassLoader());
                try{
                    ReloadingWicketFilter.this.init(filterConfig);
                }
                catch(ServletException e){
                    throw new RuntimeException((Throwable)e);
                }
            }
        });
        super.init(isServlet,filterConfig);
    }
}
