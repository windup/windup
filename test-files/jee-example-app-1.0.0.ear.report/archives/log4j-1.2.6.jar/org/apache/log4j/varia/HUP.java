package org.apache.log4j.varia;

import java.net.Socket;
import org.apache.log4j.varia.HUPNode;
import org.apache.log4j.helpers.LogLog;
import java.net.ServerSocket;
import org.apache.log4j.varia.ExternallyRolledFileAppender;

class HUP extends Thread{
    int port;
    ExternallyRolledFileAppender er;
    HUP(final ExternallyRolledFileAppender er,final int port){
        super();
        this.er=er;
        this.port=port;
    }
    public void run(){
        while(!this.isInterrupted()){
            try{
                final ServerSocket serverSocket=new ServerSocket(this.port);
                while(true){
                    final Socket socket=serverSocket.accept();
                    LogLog.debug("Connected to client at "+socket.getInetAddress());
                    new Thread(new HUPNode(socket,this.er)).start();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
