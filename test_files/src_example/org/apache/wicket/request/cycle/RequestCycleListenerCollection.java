package org.apache.wicket.request.cycle;

import org.apache.wicket.util.listener.*;
import java.util.*;
import org.apache.wicket.request.*;
import org.slf4j.*;

public class RequestCycleListenerCollection extends ListenerCollection<IRequestCycleListener> implements IRequestCycleListener{
    private static final Logger logger;
    private static final long serialVersionUID=1L;
    public void onBeginRequest(final RequestCycle cycle){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onBeginRequest(cycle);
            }
        });
    }
    public void onEndRequest(final RequestCycle cycle){
        this.reversedNotify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onEndRequest(cycle);
            }
        });
    }
    public void onDetach(final RequestCycle cycle){
        this.reversedNotifyIgnoringExceptions((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onDetach(cycle);
            }
        });
    }
    public IRequestHandler onException(final RequestCycle cycle,final Exception ex){
        final List<IRequestHandler> handlers=(List<IRequestHandler>)new ArrayList();
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                final IRequestHandler handler=listener.onException(cycle,ex);
                if(handler!=null){
                    handlers.add(handler);
                }
            }
        });
        if(handlers.isEmpty()){
            return null;
        }
        if(handlers.size()>1){
            RequestCycleListenerCollection.logger.debug("{} exception handlers available for exception {}, using the first handler",(Object)handlers.size(),ex);
        }
        return (IRequestHandler)handlers.get(0);
    }
    public void onRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onRequestHandlerResolved(cycle,handler);
            }
        });
    }
    public void onExceptionRequestHandlerResolved(final RequestCycle cycle,final IRequestHandler handler,final Exception exception){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onExceptionRequestHandlerResolved(cycle,handler,exception);
            }
        });
    }
    public void onRequestHandlerScheduled(final RequestCycle cycle,final IRequestHandler handler){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onRequestHandlerScheduled(cycle,handler);
            }
        });
    }
    public void onRequestHandlerExecuted(final RequestCycle cycle,final IRequestHandler handler){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onRequestHandlerExecuted(cycle,handler);
            }
        });
    }
    public void onUrlMapped(final RequestCycle cycle,final IRequestHandler handler,final Url url){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IRequestCycleListener>(){
            public void notify(final IRequestCycleListener listener){
                listener.onUrlMapped(cycle,handler,url);
            }
        });
    }
    static{
        logger=LoggerFactory.getLogger(RequestCycleListenerCollection.class);
    }
}
