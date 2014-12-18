package org.apache.log4j.net;

import java.net.ConnectException;
import org.apache.log4j.spi.LoggingEvent;
import java.net.Socket;
import java.io.IOException;
import org.apache.log4j.helpers.LogLog;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import org.apache.log4j.AppenderSkeleton;

public class SocketAppender extends AppenderSkeleton{
    static final int DEFAULT_PORT=4560;
    static final int DEFAULT_RECONNECTION_DELAY=30000;
    String remoteHost;
    InetAddress address;
    int port;
    ObjectOutputStream oos;
    int reconnectionDelay;
    boolean locationInfo;
    private Connector connector;
    int counter;
    private static final int RESET_FREQUENCY=1;
    public SocketAppender(){
        super();
        this.port=4560;
        this.reconnectionDelay=30000;
        this.locationInfo=false;
        this.counter=0;
    }
    public SocketAppender(final InetAddress address,final int port){
        super();
        this.port=4560;
        this.reconnectionDelay=30000;
        this.locationInfo=false;
        this.counter=0;
        this.address=address;
        this.remoteHost=address.getHostName();
        this.connect(address,this.port=port);
    }
    public SocketAppender(final String host,final int port){
        super();
        this.port=4560;
        this.reconnectionDelay=30000;
        this.locationInfo=false;
        this.counter=0;
        this.port=port;
        this.address=getAddressByName(host);
        this.remoteHost=host;
        this.connect(this.address,port);
    }
    public void activateOptions(){
        this.connect(this.address,this.port);
    }
    public synchronized void close(){
        if(super.closed){
            return;
        }
        super.closed=true;
        this.cleanUp();
    }
    public void cleanUp(){
        if(this.oos!=null){
            try{
                this.oos.close();
            }
            catch(IOException e){
                LogLog.error("Could not close oos.",e);
            }
            this.oos=null;
        }
        if(this.connector!=null){
            this.connector.interrupted=true;
            this.connector=null;
        }
    }
    void connect(final InetAddress address,final int port){
        if(this.address==null){
            return;
        }
        try{
            this.cleanUp();
            this.oos=new ObjectOutputStream(new Socket(address,port).getOutputStream());
        }
        catch(IOException e){
            LogLog.error("Could not connect to remote log4j server at ["+address.getHostName()+"]. We will try again later.",e);
            this.fireConnector();
        }
    }
    public void append(final LoggingEvent event){
        if(event==null){
            return;
        }
        if(this.address==null){
            super.errorHandler.error("No remote host is set for SocketAppender named \""+super.name+"\".");
            return;
        }
        if(this.oos!=null){
            try{
                if(this.locationInfo){
                    event.getLocationInformation();
                }
                this.oos.writeObject(event);
                this.oos.flush();
                if(++this.counter>=1){
                    this.counter=0;
                    this.oos.reset();
                }
            }
            catch(IOException e){
                this.oos=null;
                LogLog.warn("Detected problem with connection: "+e);
                if(this.reconnectionDelay>0){
                    this.fireConnector();
                }
            }
        }
    }
    void fireConnector(){
        if(this.connector==null){
            LogLog.debug("Starting a new connector thread.");
            (this.connector=new Connector()).setDaemon(true);
            this.connector.setPriority(1);
            this.connector.start();
        }
    }
    static InetAddress getAddressByName(final String host){
        try{
            return InetAddress.getByName(host);
        }
        catch(Exception e){
            LogLog.error("Could not find address of ["+host+"].",e);
            return null;
        }
    }
    public boolean requiresLayout(){
        return false;
    }
    public void setRemoteHost(final String host){
        this.address=getAddressByName(host);
        this.remoteHost=host;
    }
    public String getRemoteHost(){
        return this.remoteHost;
    }
    public void setPort(final int port){
        this.port=port;
    }
    public int getPort(){
        return this.port;
    }
    public void setLocationInfo(final boolean locationInfo){
        this.locationInfo=locationInfo;
    }
    public boolean getLocationInfo(){
        return this.locationInfo;
    }
    public void setReconnectionDelay(final int delay){
        this.reconnectionDelay=delay;
    }
    public int getReconnectionDelay(){
        return this.reconnectionDelay;
    }
    class Connector extends Thread{
        boolean interrupted;
        Connector(){
            super();
            this.interrupted=false;
        }
        public void run(){
            while(!this.interrupted){
                try{
                    Thread.sleep(SocketAppender.this.reconnectionDelay);
                    LogLog.debug("Attempting connection to "+SocketAppender.this.address.getHostName());
                    final Socket socket=new Socket(SocketAppender.this.address,SocketAppender.this.port);
                    synchronized(this){
                        SocketAppender.this.oos=new ObjectOutputStream(socket.getOutputStream());
                        SocketAppender.this.connector=null;
                        LogLog.debug("Connection established. Exiting connector thread.");
                    }
                }
                catch(InterruptedException e2){
                    LogLog.debug("Connector interrupted. Leaving loop.");
                }
                catch(ConnectException e3){
                    LogLog.debug("Remote host "+SocketAppender.this.address.getHostName()+" refused connection.");
                }
                catch(IOException e){
                    LogLog.debug("Could not connect to "+SocketAppender.this.address.getHostName()+". Exception is "+e);
                }
            }
        }
    }
}
