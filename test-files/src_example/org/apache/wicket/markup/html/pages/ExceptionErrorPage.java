package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.basic.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.html.debug.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.http.*;

public class ExceptionErrorPage extends AbstractErrorPage{
    private static final long serialVersionUID=1L;
    private final transient Throwable throwable;
    public ExceptionErrorPage(final Throwable throwable,final Page page){
        super();
        this.throwable=throwable;
        this.add(new MultiLineLabel("exception",this.getErrorMessage(throwable)));
        this.add(new MultiLineLabel("stacktrace",this.getStackTrace(throwable)));
        String resource="";
        String markup="";
        MarkupStream markupStream=null;
        if(throwable instanceof MarkupException){
            markupStream=((MarkupException)throwable).getMarkupStream();
            if(markupStream!=null){
                markup=markupStream.toHtmlDebugString();
                resource=markupStream.getResource().toString();
            }
        }
        final MultiLineLabel markupLabel=new MultiLineLabel("markup",markup);
        markupLabel.setEscapeModelStrings(false);
        final WebMarkupContainer markupHighlight=new WebMarkupContainer("markupHighlight");
        markupHighlight.add(markupLabel);
        markupHighlight.add(new Label("resource",resource));
        this.add(markupHighlight);
        markupHighlight.setVisible(markupStream!=null);
        this.add(new Link<Void>("displayPageViewLink"){
            private static final long serialVersionUID=1L;
            public void onClick(){
                ExceptionErrorPage.this.replace(new PageView("componentTree",page));
                this.setVisible(false);
            }
        });
        this.add(new Label("componentTree",""));
    }
    public String getErrorMessage(final Throwable throwable){
        if(throwable!=null){
            final StringBuilder sb=new StringBuilder(256);
            final List<Throwable> al=this.convertToList(throwable);
            final int length=al.size()-1;
            final Throwable cause=(Throwable)al.get(length);
            sb.append("Last cause: ").append(cause.getMessage()).append('\n');
            if(throwable instanceof WicketRuntimeException){
                String msg=throwable.getMessage();
                if(msg!=null&&!msg.equals(cause.getMessage())){
                    if(throwable instanceof MarkupException){
                        final MarkupStream stream=((MarkupException)throwable).getMarkupStream();
                        if(stream!=null){
                            final String text="\n"+stream.toString();
                            if(msg.endsWith(text)){
                                msg=msg.substring(0,msg.length()-text.length());
                            }
                        }
                    }
                    sb.append("WicketMessage: ");
                    sb.append(msg);
                    sb.append("\n\n");
                }
            }
            return sb.toString();
        }
        return "[Unknown]";
    }
    public String getStackTrace(final Throwable throwable){
        if(throwable!=null){
            final List<Throwable> al=this.convertToList(throwable);
            final StringBuilder sb=new StringBuilder(256);
            final int length=al.size()-1;
            final Throwable cause=(Throwable)al.get(length);
            sb.append("Root cause:\n\n");
            this.outputThrowable(cause,sb,false);
            if(length>0){
                sb.append("\n\nComplete stack:\n\n");
                for(int i=0;i<length;++i){
                    this.outputThrowable((Throwable)al.get(i),sb,true);
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        return "<Null Throwable>";
    }
    private List<Throwable> convertToList(final Throwable throwable){
        final List<Throwable> al=(List<Throwable>)Generics.newArrayList();
        Throwable cause=throwable;
        al.add(cause);
        while(cause.getCause()!=null&&cause!=cause.getCause()){
            cause=cause.getCause();
            al.add(cause);
        }
        return al;
    }
    private void outputThrowable(final Throwable cause,final StringBuilder sb,final boolean stopAtWicketServlet){
        sb.append(cause);
        sb.append("\n");
        final StackTraceElement[] trace=cause.getStackTrace();
        for(int i=0;i<trace.length;++i){
            final String traceString=trace[i].toString();
            if(!traceString.startsWith("sun.reflect.")||i<=1){
                sb.append("     at ");
                sb.append(traceString);
                sb.append("\n");
                if(stopAtWicketServlet&&(traceString.startsWith("org.apache.wicket.protocol.http.WicketServlet")||traceString.startsWith("org.apache.wicket.protocol.http.WicketFilter"))){
                    return;
                }
            }
        }
    }
    protected void setHeaders(final WebResponse response){
        response.setStatus(500);
    }
    public Throwable getThrowable(){
        return this.throwable;
    }
}
