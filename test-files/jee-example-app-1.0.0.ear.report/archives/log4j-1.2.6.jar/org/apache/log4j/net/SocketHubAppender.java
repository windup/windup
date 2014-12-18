package org.apache.log4j.net;

import java.net.InetAddress;
import java.net.Socket;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.ServerSocket;
import org.apache.log4j.spi.LoggingEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.log4j.helpers.LogLog;
import java.util.Vector;
import org.apache.log4j.AppenderSkeleton;

public class SocketHubAppender extends AppenderSkeleton{
    static final int DEFAULT_PORT=4560;
    private int port;
    private Vector oosList;
    private ServerMonitor serverMonitor;
    private boolean locationInfo;
    public SocketHubAppender(){
        super();
        this.port=4560;
        this.oosList=new Vector();
        this.serverMonitor=null;
        this.locationInfo=false;
    }
    public SocketHubAppender(final int _port){
        super();
        this.port=4560;
        this.oosList=new Vector();
        this.serverMonitor=null;
        this.locationInfo=false;
        this.port=_port;
        this.startServer();
    }
    public void activateOptions(){
        this.startServer();
    }
    public synchronized void close(){
        if(super.closed){
            return;
        }
        LogLog.debug("closing SocketHubAppender "+this.getName());
        super.closed=true;
        this.cleanUp();
        LogLog.debug("SocketHubAppender "+this.getName()+" closed");
    }
    public void cleanUp(){
        LogLog.debug("stopping ServerSocket");
        this.serverMonitor.stopMonitor();
        this.serverMonitor=null;
        LogLog.debug("closing client connections");
        while(this.oosList.size()!=0){
            final ObjectOutputStream oos=this.oosList.elementAt(0);
            if(oos!=null){
                try{
                    oos.close();
                }
                catch(IOException e){
                    LogLog.error("could not close oos.",e);
                }
                this.oosList.removeElementAt(0);
            }
        }
    }
    public void append(final LoggingEvent event){
        if(event==null||this.oosList.size()==0){
            return;
        }
        if(this.locationInfo){
            event.getLocationInformation();
        }
        for(int streamCount=0;streamCount<this.oosList.size();++streamCount){
            ObjectOutputStream oos=null;
            try{
                oos=this.oosList.elementAt(streamCount);
            }
            catch(ArrayIndexOutOfBoundsException ex){
            }
            if(oos==null){
                break;
            }
            try{
                oos.writeObject(event);
                oos.flush();
                oos.reset();
            }
            catch(IOException e){
                this.oosList.removeElementAt(streamCount);
                LogLog.debug("dropped connection");
                --streamCount;
            }
        }
    }
    public boolean requiresLayout(){
        return false;
    }
    public void setPort(final int _port){
        this.port=_port;
    }
    public int getPort(){
        return this.port;
    }
    public void setLocationInfo(final boolean _locationInfo){
        this.locationInfo=_locationInfo;
    }
    public boolean getLocationInfo(){
        return this.locationInfo;
    }
    private void startServer(){
        this.serverMonitor=new ServerMonitor(this.port,this.oosList);
    }
    private class ServerMonitor implements Runnable{
        private int port;
        private Vector oosList;
        private boolean keepRunning;
        private Thread monitorThread;
        public ServerMonitor(final int _port,final Vector _oosList){
            super();
            this.port=_port;
            this.oosList=_oosList;
            this.keepRunning=true;
            (this.monitorThread=new Thread(this)).setDaemon(true);
            this.monitorThread.start();
        }
        public synchronized void stopMonitor(){
            if(this.keepRunning){
                LogLog.debug("server monitor thread shutting down");
                this.keepRunning=false;
                try{
                    this.monitorThread.join();
                }
                catch(InterruptedException ex){
                }
                this.monitorThread=null;
                LogLog.debug("server monitor thread shut down");
            }
        }
        public void run(){
            ServerSocket serverSocket=null;
            try{
                serverSocket=new ServerSocket(this.port);
                serverSocket.setSoTimeout(1000);
            }
            catch(Exception e){
                LogLog.error("exception setting timeout, shutting down server socket.",e);
                this.keepRunning=false;
                return;
            }
            try{
                try{
                    serverSocket.setSoTimeout(1000);
                }
                catch(SocketException e2){
                    LogLog.error("exception setting timeout, shutting down server socket.",e2);
                    try{
                        serverSocket.close();
                    }
                    catch(IOException ex){
                    }
                    return;
                }
                while(this.keepRunning){
                    Socket socket=null;
                    try{
                        socket=serverSocket.accept();
                    }
                    catch(InterruptedIOException e6){
                    }
                    catch(SocketException e3){
                        LogLog.error("exception accepting socket, shutting down server socket.",e3);
                        this.keepRunning=false;
                    }
                    catch(IOException e4){
                        LogLog.error("exception accepting socket.",e4);
                    }
                    if(socket!=null){
                        try{
                            final InetAddress remoteAddress=socket.getInetAddress();
                            LogLog.debug("accepting connection from "+remoteAddress.getHostName()+" ("+remoteAddress.getHostAddress()+")");
                            final ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
                            this.oosList.addElement(oos);
                        }
                        catch(IOException e5){
                            LogLog.error("exception creating output stream on socket.",e5);
                        }
                    }
                }
                try{
                    serverSocket.close();
                }
                catch(IOException ex2){
                }
            }
            finally{
                try{
                    serverSocket.close();
                }
                catch(IOException ex3){
                }
            }
        }
    }
}
