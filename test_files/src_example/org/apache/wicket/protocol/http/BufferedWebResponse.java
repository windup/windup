package org.apache.wicket.protocol.http;

import org.apache.wicket.request.http.*;
import javax.servlet.http.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;
import org.apache.wicket.response.filter.*;
import java.io.*;

public class BufferedWebResponse extends WebResponse implements IMetaDataBufferingWebResponse{
    private final WebResponse originalResponse;
    private final List<Action> actions;
    private WriteCharSequenceAction charSequenceAction;
    private WriteDataAction dataAction;
    public BufferedWebResponse(final WebResponse originalResponse){
        super();
        this.actions=(List<Action>)new ArrayList();
        if(originalResponse instanceof IMetaDataBufferingWebResponse){
            ((IMetaDataBufferingWebResponse)originalResponse).writeMetaData(this);
        }
        this.originalResponse=originalResponse;
    }
    public void writeMetaData(final WebResponse response){
        for(final Action action : this.actions){
            if(action instanceof MetaDataAction){
                action.invoke(response);
            }
        }
    }
    public String encodeURL(final CharSequence url){
        if(this.originalResponse!=null){
            return this.originalResponse.encodeURL(url);
        }
        return (url!=null)?url.toString():null;
    }
    public String encodeRedirectURL(final CharSequence url){
        if(this.originalResponse!=null){
            return this.originalResponse.encodeRedirectURL(url);
        }
        return (url!=null)?url.toString():null;
    }
    public void reset(){
        super.reset();
        this.actions.clear();
        this.charSequenceAction=null;
        this.dataAction=null;
    }
    public void addCookie(final Cookie cookie){
        this.actions.add(new AddCookieAction(cookie));
    }
    public void clearCookie(final Cookie cookie){
        this.actions.add(new ClearCookieAction(cookie));
    }
    public void setContentLength(final long length){
        this.actions.add(new SetContentLengthAction(length));
    }
    public void setContentType(final String mimeType){
        this.actions.add(new SetContentTypeAction(mimeType));
    }
    public void setDateHeader(final String name,final Time date){
        this.actions.add(new SetDateHeaderAction(name,date));
    }
    public void setHeader(final String name,final String value){
        this.actions.add(new SetHeaderAction(name,value));
    }
    public void addHeader(final String name,final String value){
        this.actions.add(new AddHeaderAction(name,value));
    }
    public void write(final CharSequence sequence){
        if(this.dataAction!=null){
            throw new IllegalStateException("Can't call write(CharSequence) after write(byte[]) has been called.");
        }
        if(this.charSequenceAction==null){
            this.charSequenceAction=new WriteCharSequenceAction();
            this.actions.add(this.charSequenceAction);
        }
        this.charSequenceAction.append(sequence);
    }
    public CharSequence getText(){
        if(this.dataAction!=null){
            throw new IllegalStateException("write(byte[]) has already been called.");
        }
        if(this.charSequenceAction!=null){
            return this.charSequenceAction.builder;
        }
        return null;
    }
    public void setText(final CharSequence text){
        if(this.dataAction!=null){
            throw new IllegalStateException("write(byte[]) has already been called.");
        }
        if(this.charSequenceAction!=null){
            this.charSequenceAction.builder.setLength(0);
        }
        this.write(text);
    }
    public void write(final byte[] array){
        if(this.charSequenceAction!=null){
            throw new IllegalStateException("Can't call write(byte[]) after write(CharSequence) has been called.");
        }
        if(this.dataAction==null){
            this.dataAction=new WriteDataAction();
            this.actions.add(this.dataAction);
        }
        this.dataAction.append(array);
    }
    public void write(final byte[] array,final int offset,final int length){
        if(this.charSequenceAction!=null){
            throw new IllegalStateException("Can't call write(byte[]) after write(CharSequence) has been called.");
        }
        if(this.dataAction==null){
            this.dataAction=new WriteDataAction();
            this.actions.add(this.dataAction);
        }
        this.dataAction.append(array,offset,length);
    }
    public void sendRedirect(final String url){
        this.actions.add(new SendRedirectAction(url));
    }
    public void setStatus(final int sc){
        this.actions.add(new SetStatusAction(sc));
    }
    public void sendError(final int sc,final String msg){
        this.actions.add(new SendErrorAction(sc,msg));
    }
    public void writeTo(final WebResponse response){
        Args.notNull((Object)response,"response");
        Collections.sort(this.actions);
        for(final Action action : this.actions){
            action.invoke(response);
        }
    }
    public boolean isRedirect(){
        for(final Action action : this.actions){
            if(action instanceof SendRedirectAction){
                return true;
            }
        }
        return false;
    }
    public void flush(){
        this.actions.add(new FlushAction());
    }
    private static final void writeStream(final Response response,final ByteArrayOutputStream stream){
        final boolean[] copied= { false };
        try{
            stream.writeTo(new OutputStream(){
                public void write(final int b) throws IOException{
                }
                public void write(final byte[] b,final int off,final int len) throws IOException{
                    if(off==0&&len==b.length){
                        response.write(b);
                        copied[0]=true;
                    }
                }
            });
        }
        catch(IOException e1){
            throw new WicketRuntimeException(e1);
        }
        if(!copied[0]){
            response.write(stream.toByteArray());
        }
    }
    public String toString(){
        String toString;
        if(this.charSequenceAction!=null){
            toString=this.charSequenceAction.builder.toString();
        }
        else{
            toString=super.toString();
        }
        return toString;
    }
    public Object getContainerResponse(){
        return this.originalResponse.getContainerResponse();
    }
    private abstract static class Action implements Comparable<Action>{
        protected abstract void invoke(final WebResponse p0);
        public int compareTo(final Action o){
            return 0;
        }
    }
    private abstract static class MetaDataAction extends Action{
        public int compareTo(final Action o){
            return -1;
        }
    }
    private static class WriteCharSequenceAction extends Action{
        private final StringBuilder builder;
        public WriteCharSequenceAction(){
            super();
            this.builder=new StringBuilder(4096);
        }
        public void append(final CharSequence sequence){
            this.builder.append(sequence);
        }
        protected void invoke(final WebResponse response){
            AppendingStringBuffer responseBuffer=new AppendingStringBuffer((CharSequence)this.builder);
            final List<IResponseFilter> responseFilters=Application.get().getRequestCycleSettings().getResponseFilters();
            if(responseFilters!=null){
                for(final IResponseFilter filter : responseFilters){
                    responseBuffer=filter.filter(responseBuffer);
                }
            }
            response.write((CharSequence)responseBuffer);
        }
        public int compareTo(final Action o){
            return 1;
        }
    }
    private static class WriteDataAction extends Action{
        private final ByteArrayOutputStream stream;
        public WriteDataAction(){
            super();
            this.stream=new ByteArrayOutputStream();
        }
        public void append(final byte[] data){
            try{
                this.stream.write(data);
            }
            catch(IOException e){
                throw new WicketRuntimeException(e);
            }
        }
        public void append(final byte[] data,final int offset,final int length){
            try{
                this.stream.write(data,offset,length);
            }
            catch(Exception e){
                throw new WicketRuntimeException(e);
            }
        }
        protected void invoke(final WebResponse response){
            writeStream((Response)response,this.stream);
        }
        public int compareTo(final Action o){
            return 1;
        }
    }
    private static class CloseAction extends Action{
        protected void invoke(final WebResponse response){
            response.close();
        }
    }
    private static class AddCookieAction extends MetaDataAction{
        private final Cookie cookie;
        public AddCookieAction(final Cookie cookie){
            super();
            this.cookie=cookie;
        }
        protected void invoke(final WebResponse response){
            response.addCookie(this.cookie);
        }
    }
    private static class ClearCookieAction extends MetaDataAction{
        private final Cookie cookie;
        public ClearCookieAction(final Cookie cookie){
            super();
            this.cookie=cookie;
        }
        protected void invoke(final WebResponse response){
            response.clearCookie(this.cookie);
        }
    }
    private static class SetHeaderAction extends MetaDataAction{
        private final String name;
        private final String value;
        public SetHeaderAction(final String name,final String value){
            super();
            this.name=name;
            this.value=value;
        }
        protected void invoke(final WebResponse response){
            response.setHeader(this.name,this.value);
        }
    }
    private static class AddHeaderAction extends MetaDataAction{
        private final String name;
        private final String value;
        public AddHeaderAction(final String name,final String value){
            super();
            this.name=name;
            this.value=value;
        }
        protected void invoke(final WebResponse response){
            response.addHeader(this.name,this.value);
        }
    }
    private static class SetDateHeaderAction extends MetaDataAction{
        private final String name;
        private final Time value;
        public SetDateHeaderAction(final String name,final Time value){
            super();
            this.name=name;
            this.value=(Time)Args.notNull((Object)value,"value");
        }
        protected void invoke(final WebResponse response){
            response.setDateHeader(this.name,this.value);
        }
    }
    private static class SetContentLengthAction extends MetaDataAction{
        private final long contentLength;
        public SetContentLengthAction(final long contentLength){
            super();
            this.contentLength=contentLength;
        }
        protected void invoke(final WebResponse response){
            response.setContentLength(this.contentLength);
        }
    }
    private static class SetContentTypeAction extends MetaDataAction{
        private final String contentType;
        public SetContentTypeAction(final String contentType){
            super();
            this.contentType=contentType;
        }
        protected void invoke(final WebResponse response){
            response.setContentType(this.contentType);
        }
    }
    private static class SetStatusAction extends MetaDataAction{
        private final int sc;
        public SetStatusAction(final int sc){
            super();
            this.sc=sc;
        }
        protected void invoke(final WebResponse response){
            response.setStatus(this.sc);
        }
    }
    private static class SendErrorAction extends Action{
        private final int sc;
        private final String msg;
        public SendErrorAction(final int sc,final String msg){
            super();
            this.sc=sc;
            this.msg=msg;
        }
        protected void invoke(final WebResponse response){
            response.sendError(this.sc,this.msg);
        }
    }
    private static class SendRedirectAction extends Action{
        private final String url;
        public SendRedirectAction(final String url){
            super();
            this.url=url;
        }
        protected void invoke(final WebResponse response){
            response.sendRedirect(this.url);
        }
    }
    private static class FlushAction extends Action{
        protected void invoke(final WebResponse response){
            response.flush();
        }
    }
}
