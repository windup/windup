package org.apache.log4j.helpers;

import java.net.DatagramPacket;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.log4j.helpers.LogLog;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.Writer;

public class SyslogWriter extends Writer{
    final int SYSLOG_PORT=514;
    static String syslogHost;
    private InetAddress address;
    private DatagramSocket ds;
    public SyslogWriter(final String syslogHost){
        super();
        SyslogWriter.syslogHost=syslogHost;
        try{
            this.address=InetAddress.getByName(syslogHost);
        }
        catch(UnknownHostException e){
            LogLog.error("Could not find "+syslogHost+". All logging will FAIL.",e);
        }
        try{
            this.ds=new DatagramSocket();
        }
        catch(SocketException e2){
            e2.printStackTrace();
            LogLog.error("Could not instantiate DatagramSocket to "+syslogHost+". All logging will FAIL.",e2);
        }
    }
    public void write(final char[] buf,final int off,final int len) throws IOException{
        this.write(new String(buf,off,len));
    }
    public void write(final String string) throws IOException{
        final byte[] bytes=string.getBytes();
        final DatagramPacket packet=new DatagramPacket(bytes,bytes.length,this.address,514);
        if(this.ds!=null){
            this.ds.send(packet);
        }
    }
    public void flush(){
    }
    public void close(){
    }
}
