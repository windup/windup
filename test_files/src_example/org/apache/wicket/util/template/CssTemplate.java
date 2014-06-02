package org.apache.wicket.util.template;

import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.css.*;

public final class CssTemplate extends TextTemplateDecorator{
    private static final long serialVersionUID=1L;
    public CssTemplate(final TextTemplate textTemplate){
        super(textTemplate);
    }
    public String getBeforeTemplateContents(){
        return "<style type=\"text/css\"><!--\n";
    }
    public String getAfterTemplateContents(){
        return "--></style>\n";
    }
    public TextTemplate interpolate(final Map<String,?> variables){
        return this;
    }
    public String getString(){
        final String nonCompressed=super.getString();
        ICssCompressor compressor=null;
        if(Application.exists()){
            compressor=Application.get().getResourceSettings().getCssCompressor();
        }
        if(compressor!=null){
            return compressor.compress(nonCompressed);
        }
        return nonCompressed;
    }
}
