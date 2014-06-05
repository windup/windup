package org.apache.wicket.request.resource;

import java.util.*;
import org.apache.wicket.css.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class CssPackageResource extends PackageResource{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    public CssPackageResource(final Class<?> scope,final String name,final Locale locale,final String style,final String variation){
        super(scope,name,locale,style,variation);
    }
    protected byte[] processResponse(final IResource.Attributes attributes,final byte[] bytes){
        final byte[] processedResponse=super.processResponse(attributes,bytes);
        final ICssCompressor compressor=this.getCompressor();
        if(compressor!=null){
            try{
                final String nonCompressed=new String(processedResponse,"UTF-8");
                return compressor.compress(nonCompressed).getBytes();
            }
            catch(Exception e){
                CssPackageResource.log.error("Error while filtering content",e);
                return processedResponse;
            }
        }
        return processedResponse;
    }
    protected ICssCompressor getCompressor(){
        ICssCompressor compressor=null;
        if(Application.exists()){
            compressor=Application.get().getResourceSettings().getCssCompressor();
        }
        return compressor;
    }
    static{
        log=LoggerFactory.getLogger(CssPackageResource.class);
    }
}
