package org.apache.log4j.lf5;

import org.apache.log4j.lf5.viewer.LogBrokerMonitor;

public class AppenderFinalizer{
    protected LogBrokerMonitor _defaultMonitor;
    public AppenderFinalizer(final LogBrokerMonitor defaultMonitor){
        super();
        this._defaultMonitor=null;
        this._defaultMonitor=defaultMonitor;
    }
    protected void finalize() throws Throwable{
        System.out.println("Disposing of the default LogBrokerMonitor instance");
        this._defaultMonitor.dispose();
    }
}
