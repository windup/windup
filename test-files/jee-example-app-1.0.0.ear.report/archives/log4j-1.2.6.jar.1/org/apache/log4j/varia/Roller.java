package org.apache.log4j.varia;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;

public class Roller{
    static Category cat;
    static String host;
    static int port;
    static /* synthetic */ Class class$org$apache$log4j$varia$Roller;
    public static void main(final String[] argv){
        BasicConfigurator.configure();
        if(argv.length==2){
            init(argv[0],argv[1]);
        }
        else{
            usage("Wrong number of arguments.");
        }
        roll();
    }
    static void usage(final String msg){
        System.err.println(msg);
        System.err.println("Usage: java "+((Roller.class$org$apache$log4j$varia$Roller==null)?(Roller.class$org$apache$log4j$varia$Roller=class$("org.apache.log4j.varia.Roller")):Roller.class$org$apache$log4j$varia$Roller).getName()+"host_name port_number");
        System.exit(1);
    }
    static void init(final String hostArg,final String portArg){
        Roller.host=hostArg;
        try{
            Roller.port=Integer.parseInt(portArg);
        }
        catch(NumberFormatException e){
            usage("Second argument "+portArg+" is not a valid integer.");
        }
    }
    static void roll(){
        try{
            final Socket socket=new Socket(Roller.host,Roller.port);
            final DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
            final DataInputStream dis=new DataInputStream(socket.getInputStream());
            dos.writeUTF("RollOver");
            final String rc=dis.readUTF();
            if("OK".equals(rc)){
                Roller.cat.info("Roll over signal acknowledged by remote appender.");
            }
            else{
                Roller.cat.warn("Unexpected return code "+rc+" from remote entity.");
                System.exit(2);
            }
        }
        catch(IOException e){
            Roller.cat.error("Could not send roll signal on host "+Roller.host+" port "+Roller.port+" .",e);
            System.exit(2);
        }
        System.exit(0);
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
        Roller.cat=Category.getInstance(((Roller.class$org$apache$log4j$varia$Roller==null)?(Roller.class$org$apache$log4j$varia$Roller=class$("org.apache.log4j.varia.Roller")):Roller.class$org$apache$log4j$varia$Roller).getName());
    }
}
