package org.apache.wicket.response.filter;

import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class XmlCleaningResponseFilter implements IResponseFilter{
    private static final Logger LOG;
    public AppendingStringBuffer filter(final AppendingStringBuffer responseBuffer){
        AppendingStringBuffer result=responseBuffer;
        if(this.shouldFilter(responseBuffer)){
            result=this.stripNonValidXMLCharacters(responseBuffer);
        }
        return result;
    }
    protected boolean shouldFilter(final AppendingStringBuffer responseBuffer){
        final int min=Math.min(150,responseBuffer.length());
        final String firstNChars=responseBuffer.substring(0,min);
        return firstNChars.contains((CharSequence)"<ajax-response>");
    }
    public AppendingStringBuffer stripNonValidXMLCharacters(final AppendingStringBuffer input){
        final char[] chars=input.getValue();
        AppendingStringBuffer out=null;
        final boolean isDebugEnabled=XmlCleaningResponseFilter.LOG.isDebugEnabled();
        int codePoint;
        for(int i=0;i<input.length();i+=Character.charCount(codePoint)){
            codePoint=Character.codePointAt(chars,i,chars.length);
            if(!this.isValidXmlChar(codePoint)){
                if(out==null){
                    out=new AppendingStringBuffer(chars.length);
                    out.append((Object)input.subSequence(0,i));
                    if(isDebugEnabled){
                        XmlCleaningResponseFilter.LOG.debug("An invalid character '{}' found at position '{}' in '{}'",String.format("0x%X",new Object[] { codePoint }),i,new String(chars));
                    }
                }
                else{
                    out.append(Character.toChars(codePoint));
                    if(isDebugEnabled){
                        XmlCleaningResponseFilter.LOG.debug(String.format("Dropping character for codePoint '0x%X' at position '%d'",new Object[] { codePoint,i }));
                    }
                }
            }
            else if(out!=null){
                out.append(Character.toChars(codePoint));
            }
        }
        return (out!=null)?out:input;
    }
    protected boolean isValidXmlChar(final int codePoint){
        return codePoint==9||codePoint==10||codePoint==13||(codePoint>=32&&codePoint<=55295)||(codePoint>=57344&&codePoint<=65533)||(codePoint>=65536&&codePoint<=1114111);
    }
    static{
        LOG=LoggerFactory.getLogger(XmlCleaningResponseFilter.class);
    }
}
