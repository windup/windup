package org.apache.wicket.util.template;

import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.resource.*;

public final class JavaScriptTemplate extends TextTemplateDecorator{
    private static final long serialVersionUID=1L;
    public JavaScriptTemplate(final TextTemplate textTemplate){
        super(textTemplate);
    }
    public String getBeforeTemplateContents(){
        return "<script type=\"text/javascript\">\n/*<![CDATA[*/\n";
    }
    public String getAfterTemplateContents(){
        return "\n/*]]>*/\n</script>\n";
    }
    public TextTemplate interpolate(final Map<String,?> variables){
        return this;
    }
    public String getString(){
        final String nonCompressed=super.getString();
        ITextResourceCompressor compressor=null;
        if(Application.exists()){
            compressor=Application.get().getResourceSettings().getJavaScriptCompressor();
        }
        if(compressor!=null){
            return compressor.compress(nonCompressed);
        }
        return nonCompressed;
    }
}
