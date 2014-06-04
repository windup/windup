package org.apache.wicket.request.resource;

import java.util.*;
import org.apache.wicket.javascript.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class JavaScriptPackageResource extends PackageResource{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    public JavaScriptPackageResource(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    protected byte[] processResponse(final IResource.Attributes attributes,final byte[] bytes){
        final byte[] processedResponse=super.processResponse(attributes,bytes);
        final IJavaScriptCompressor compressor=this.getCompressor();
        if(compressor!=null){
            try{
                final String nonCompressed=new String(processedResponse,"UTF-8");
                return compressor.compress(nonCompressed).getBytes("UTF-8");
            }
            catch(Exception e){
                JavaScriptPackageResource.log.error("Error while filtering content",e);
                return processedResponse;
            }
        }
        return processedResponse;
    }
    protected IJavaScriptCompressor getCompressor(){
        IJavaScriptCompressor compressor=null;
        if(Application.exists()){
            compressor=Application.get().getResourceSettings().getJavaScriptCompressor();
        }
        return compressor;
    }
    static{
        log=LoggerFactory.getLogger(JavaScriptPackageResource.class);
    }
}
