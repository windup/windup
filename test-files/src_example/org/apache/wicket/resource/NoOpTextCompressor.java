package org.apache.wicket.resource;

import org.apache.wicket.javascript.*;
import org.apache.wicket.css.*;

public class NoOpTextCompressor implements IJavaScriptCompressor,ICssCompressor{
    public String compress(final String original){
        return original;
    }
}
