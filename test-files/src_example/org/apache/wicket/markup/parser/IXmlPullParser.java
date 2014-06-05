package org.apache.wicket.markup.parser;

import org.apache.wicket.util.resource.*;
import java.io.*;
import java.text.*;

public interface IXmlPullParser{
    String getEncoding();
    CharSequence getDoctype();
    CharSequence getInputFromPositionMarker(int p0);
    CharSequence getInput(int p0,int p1);
    void parse(CharSequence p0) throws IOException,ResourceStreamNotFoundException;
    void parse(InputStream p0) throws IOException,ResourceStreamNotFoundException;
    void parse(InputStream p0,String p1) throws IOException;
    HttpTagType next() throws ParseException;
    XmlTag getElement();
    CharSequence getString();
    void setPositionMarker();
    void setPositionMarker(int p0);
    public enum HttpTagType{
        NOT_INITIALIZED,TAG,BODY,COMMENT,CONDITIONAL_COMMENT,CONDITIONAL_COMMENT_ENDIF,CDATA,PROCESSING_INSTRUCTION,DOCTYPE,SPECIAL_TAG;
    }
}
