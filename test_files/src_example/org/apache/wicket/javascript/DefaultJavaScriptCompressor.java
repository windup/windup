package org.apache.wicket.javascript;

import org.apache.wicket.util.string.*;

public class DefaultJavaScriptCompressor implements IJavaScriptCompressor{
    public String compress(final String original){
        return new JavaScriptStripper().stripCommentsAndWhitespace(original);
    }
}
