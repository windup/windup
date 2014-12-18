package org.apache.log4j.net;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.spi.RootCategory;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.log4j.net.SocketNode;
import java.net.ServerSocket;
import java.io.File;
import org.apache.log4j.spi.LoggerRepository;
import java.util.Hashtable;
import org.apache.log4j.Category;

public class SocketServer{
    static String GENERIC;
    static String CONFIG_FILE_EXT;
    static Category cat;
    static SocketServer server;
    static int port;
    Hashtable hierarchyMap;
    LoggerRepository genericHierarchy;
    File dir;
    static /* synthetic */ Class class$org$apache$log4j$net$SocketServer;
    public static void main(final String[] argv){
        if(argv.length==3){
            init(argv[0],argv[1],argv[2]);
        }
        else{
            usage("Wrong number of arguments.");
        }
        try{
            SocketServer.cat.info("Listening on port "+SocketServer.port);
            final ServerSocket serverSocket=new ServerSocket(SocketServer.port);
            while(true){
                SocketServer.cat.info("Waiting to accept a new client.");
                final Socket socket=serverSocket.accept();
                final InetAddress inetAddress=socket.getInetAddress();
                SocketServer.cat.info("Connected to client at "+inetAddress);
                LoggerRepository h=SocketServer.server.hierarchyMap.get(inetAddress);
                if(h==null){
                    h=SocketServer.server.configureHierarchy(inetAddress);
                }
                SocketServer.cat.info("Starting new socket node.");
                new Thread(new SocketNode(socket,h)).start();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    static void usage(final String msg){
        System.err.println(msg);
        System.err.println("Usage: java "+((SocketServer.class$org$apache$log4j$net$SocketServer==null)?(SocketServer.class$org$apache$log4j$net$SocketServer=class$("org.apache.log4j.net.SocketServer")):SocketServer.class$org$apache$log4j$net$SocketServer).getName()+" port configFile directory");
        System.exit(1);
    }
    static void init(final String portStr,final String configFile,final String dirStr){
        try{
            SocketServer.port=Integer.parseInt(portStr);
        }
        catch(NumberFormatException e){
            e.printStackTrace();
            usage("Could not interpret port number ["+portStr+"].");
        }
        PropertyConfigurator.configure(configFile);
        final File dir=new File(dirStr);
        if(!dir.isDirectory()){
            usage("["+dirStr+"] is not a directory.");
        }
        SocketServer.server=new SocketServer(dir);
    }
    public SocketServer(final File directory){
        super();
        this.dir=directory;
        this.hierarchyMap=new Hashtable(11);
    }
    LoggerRepository configureHierarchy(final InetAddress inetAddress){
        SocketServer.cat.info("Locating configuration file for "+inetAddress);
        final String s=inetAddress.toString();
        final int i=s.indexOf("/");
        if(i==-1){
            SocketServer.cat.warn("Could not parse the inetAddress ["+inetAddress+"]. Using default hierarchy.");
            return this.genericHierarchy();
        }
        final String key=s.substring(0,i);
        final File configFile=new File(this.dir,key+SocketServer.CONFIG_FILE_EXT);
        if(configFile.exists()){
            final Hierarchy h=new Hierarchy(new RootCategory((Level)Priority.DEBUG));
            this.hierarchyMap.put(inetAddress,h);
            new PropertyConfigurator().doConfigure(configFile.getAbsolutePath(),h);
            return h;
        }
        SocketServer.cat.warn("Could not find config file ["+configFile+"].");
        return this.genericHierarchy();
    }
    LoggerRepository genericHierarchy(){
        if(this.genericHierarchy==null){
            final File f=new File(this.dir,SocketServer.GENERIC+SocketServer.CONFIG_FILE_EXT);
            if(f.exists()){
                this.genericHierarchy=new Hierarchy(new RootCategory((Level)Priority.DEBUG));
                new PropertyConfigurator().doConfigure(f.getAbsolutePath(),this.genericHierarchy);
            }
            else{
                SocketServer.cat.warn("Could not find config file ["+f+"]. Will use the default hierarchy.");
                this.genericHierarchy=LogManager.getLoggerRepository();
            }
        }
        return this.genericHierarchy;
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
        SocketServer.GENERIC="generic";
        SocketServer.CONFIG_FILE_EXT=".lcf";
        SocketServer.cat=Category.getInstance((SocketServer.class$org$apache$log4j$net$SocketServer==null)?(SocketServer.class$org$apache$log4j$net$SocketServer=class$("org.apache.log4j.net.SocketServer")):SocketServer.class$org$apache$log4j$net$SocketServer);
    }
}
