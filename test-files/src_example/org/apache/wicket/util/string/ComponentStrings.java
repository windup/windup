package org.apache.wicket.util.string;

import org.apache.wicket.*;

public class ComponentStrings{
    public static String toString(final Component component,final Throwable location){
        final Class<?> componentClass=(Class<?>)component.getClass();
        String componentType=componentClass.getName();
        if(componentType.indexOf(36)>=0){
            componentType=componentClass.getSuperclass().getName();
        }
        componentType=componentType.substring(componentType.lastIndexOf(46)+1);
        final AppendingStringBuffer sb=new AppendingStringBuffer((CharSequence)("The "+componentType.toLowerCase()+" with id '"+component.getId()+"' that failed to render was "+location.getMessage()+"\n"));
        final String[] skippedElements= { "org.apache.wicket.MarkupContainer","org.apache.wicket.Component","org.apache.wicket.markup" };
        final String[] breakingElements= { "org.apache.wicket.protocol.http.WicketServlet","org.apache.wicket.protocol.http.WicketFilter","java.lang.reflect" };
        final StackTraceElement[] trace=location.getStackTrace();
        for(int i=0;i<trace.length;++i){
            final String traceString=trace[i].toString();
            if(!shouldSkip(traceString,skippedElements)){
                if(!traceString.startsWith("sun.reflect.")||i<=1){
                    if(!traceString.contains((CharSequence)"java.lang.reflect")){
                        sb.append("     at ");
                        sb.append(traceString);
                        sb.append("\n");
                    }
                    if(shouldSkip(traceString,breakingElements)){
                        break;
                    }
                }
            }
        }
        sb.append("\n");
        return sb.toString();
    }
    private static boolean shouldSkip(final String text,final String[] filters){
        for(final String filter : filters){
            if(text.contains((CharSequence)filter)){
                return true;
            }
        }
        return false;
    }
}
