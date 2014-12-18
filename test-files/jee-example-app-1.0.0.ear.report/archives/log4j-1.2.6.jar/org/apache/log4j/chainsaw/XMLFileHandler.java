package org.apache.log4j.chainsaw;

import org.apache.log4j.chainsaw.EventDetails;
import org.xml.sax.Attributes;
import java.util.StringTokenizer;
import org.xml.sax.SAXException;
import org.apache.log4j.Priority;
import org.apache.log4j.chainsaw.MyTableModel;
import org.xml.sax.helpers.DefaultHandler;

class XMLFileHandler extends DefaultHandler{
    private static final String TAG_EVENT="log4j:event";
    private static final String TAG_MESSAGE="log4j:message";
    private static final String TAG_NDC="log4j:NDC";
    private static final String TAG_THROWABLE="log4j:throwable";
    private static final String TAG_LOCATION_INFO="log4j:locationInfo";
    private final MyTableModel mModel;
    private int mNumEvents;
    private long mTimeStamp;
    private Priority mPriority;
    private String mCategoryName;
    private String mNDC;
    private String mThreadName;
    private String mMessage;
    private String[] mThrowableStrRep;
    private String mLocationDetails;
    private final StringBuffer mBuf;
    XMLFileHandler(final MyTableModel aModel){
        super();
        this.mBuf=new StringBuffer();
        this.mModel=aModel;
    }
    public void startDocument() throws SAXException{
        this.mNumEvents=0;
    }
    public void characters(final char[] aChars,final int aStart,final int aLength){
        this.mBuf.append(String.valueOf(aChars,aStart,aLength));
    }
    public void endElement(final String aNamespaceURI,final String aLocalName,final String aQName){
        if("log4j:event".equals(aQName)){
            this.addEvent();
            this.resetData();
        }
        else if("log4j:NDC".equals(aQName)){
            this.mNDC=this.mBuf.toString();
        }
        else if("log4j:message".equals(aQName)){
            this.mMessage=this.mBuf.toString();
        }
        else if("log4j:throwable".equals(aQName)){
            final StringTokenizer st=new StringTokenizer(this.mBuf.toString(),"\n\t");
            this.mThrowableStrRep=new String[st.countTokens()];
            if(this.mThrowableStrRep.length>0){
                this.mThrowableStrRep[0]=st.nextToken();
                for(int i=1;i<this.mThrowableStrRep.length;++i){
                    this.mThrowableStrRep[i]="\t"+st.nextToken();
                }
            }
        }
    }
    public void startElement(final String aNamespaceURI,final String aLocalName,final String aQName,final Attributes aAtts){
        this.mBuf.setLength(0);
        if("log4j:event".equals(aQName)){
            this.mThreadName=aAtts.getValue("thread");
            this.mTimeStamp=Long.parseLong(aAtts.getValue("timestamp"));
            this.mCategoryName=aAtts.getValue("logger");
            this.mPriority=Priority.toPriority(aAtts.getValue("level"));
        }
        else if("log4j:locationInfo".equals(aQName)){
            this.mLocationDetails=aAtts.getValue("class")+"."+aAtts.getValue("method")+"("+aAtts.getValue("file")+":"+aAtts.getValue("line")+")";
        }
    }
    int getNumEvents(){
        return this.mNumEvents;
    }
    private void addEvent(){
        this.mModel.addEvent(new EventDetails(this.mTimeStamp,this.mPriority,this.mCategoryName,this.mNDC,this.mThreadName,this.mMessage,this.mThrowableStrRep,this.mLocationDetails));
        ++this.mNumEvents;
    }
    private void resetData(){
        this.mTimeStamp=0L;
        this.mPriority=null;
        this.mCategoryName=null;
        this.mNDC=null;
        this.mThreadName=null;
        this.mMessage=null;
        this.mThrowableStrRep=null;
        this.mLocationDetails=null;
    }
}
