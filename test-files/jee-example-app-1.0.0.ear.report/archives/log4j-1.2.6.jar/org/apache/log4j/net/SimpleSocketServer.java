package org.apache.log4j.net;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import java.net.Socket;
import org.apache.log4j.net.SocketNode;
import org.apache.log4j.LogManager;
import java.net.ServerSocket;
import org.apache.log4j.Category;

public class SimpleSocketServer{
    static Category cat;
    static int port;
    static /* synthetic */ Class class$org$apache$log4j$net$SimpleSocketServer;
    public static void main(final String[] argv){
        if(argv.length==2){
            init(argv[0],argv[1]);
        }
        else{
            usage("Wrong number of arguments.");
        }
        try{
            SimpleSocketServer.cat.info("Listening on port "+SimpleSocketServer.port);
            final ServerSocket serverSocket=new ServerSocket(SimpleSocketServer.port);
            while(true){
                SimpleSocketServer.cat.info("Waiting to accept a new client.");
                final Socket socket=serverSocket.accept();
                SimpleSocketServer.cat.info("Connected to client at "+socket.getInetAddress());
                SimpleSocketServer.cat.info("Starting new socket node.");
                new Thread(new SocketNode(socket,LogManager.getLoggerRepository())).start();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    static void usage(final String msg){
        System.err.println(msg);
        System.err.println("Usage: java "+((SimpleSocketServer.class$org$apache$log4j$net$SimpleSocketServer==null)?(SimpleSocketServer.class$org$apache$log4j$net$SimpleSocketServer=class$("org.apache.log4j.net.SimpleSocketServer")):SimpleSocketServer.class$org$apache$log4j$net$SimpleSocketServer).getName()+" port configFile");
        System.exit(1);
    }
    static void init(final String portStr,final String configFile){
        try{
            SimpleSocketServer.port=Integer.parseInt(portStr);
        }
        catch(NumberFormatException e){
            e.printStackTrace();
            usage("Could not interpret port number ["+portStr+"].");
        }
        if(configFile.endsWith(".xml")){
            new DOMConfigurator();
            DOMConfigurator.configure(configFile);
        }
        else{
            new PropertyConfigurator();
            PropertyConfigurator.configure(configFile);
        }
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        SimpleSocketServer.cat=Category.getInstance(((SimpleSocketServer.class$org$apache$log4j$net$SimpleSocketServer==null)?(SimpleSocketServer.class$org$apache$log4j$net$SimpleSocketServer=class$("org.apache.log4j.net.SimpleSocketServer")):SimpleSocketServer.class$org$apache$log4j$net$SimpleSocketServer).getName());
    }
}
