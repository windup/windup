package org.apache.wicket.util.io;

import java.io.*;
import java.util.*;
import org.apache.wicket.util.string.*;

@Deprecated
public class WicketSerializeableException extends NotSerializableException{
    private static final long serialVersionUID=1L;
    private final List<String> list;
    public WicketSerializeableException(final String message){
        super(message);
        this.list=(List<String>)new ArrayList();
    }
    public WicketSerializeableException(final String message,final Throwable cause){
        this(message);
        this.initCause(cause);
    }
    public String getMessage(){
        final AppendingStringBuffer asb=new AppendingStringBuffer((CharSequence)super.getMessage());
        if(this.list.size()>0){
            asb.append("\n");
            int i=this.list.size();
            while(--i>=0){
                final String element=(String)this.list.get(i);
                asb.append(element);
                asb.append("->");
            }
            asb.setLength(asb.length()-2);
        }
        asb.append("\nNOTE: if you feel Wicket is at fault with this exception").append(", please report to the mailing list. You can switch to ").append("JDK based serialization by calling: ").append("org.apache.wicket.util.lang.Objects.setObjectStreamFactory(").append("new IObjectStreamFactory.DefaultObjectStreamFactory()) ").append("e.g. in the init method of your application");
        return asb.toString();
    }
    public void addTrace(final String traceString){
        this.list.add(traceString);
    }
}
