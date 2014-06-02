package org.apache.wicket.markup.transformer;

import javax.xml.transform.stream.*;
import java.io.*;
import org.apache.wicket.util.resource.*;
import javax.xml.transform.*;
import org.apache.wicket.*;

public class XsltTransformer implements ITransformer{
    private static final String extension="xsl";
    private final String xslFile;
    public XsltTransformer(){
        super();
        this.xslFile=null;
    }
    public XsltTransformer(final String xslFile){
        super();
        if(xslFile!=null&&xslFile.endsWith("xsl")){
            this.xslFile=xslFile.substring(0,xslFile.length()-"xsl".length()-1);
        }
        else{
            this.xslFile=xslFile;
        }
    }
    public CharSequence transform(final Component component,final CharSequence output) throws Exception{
        final IResourceStream resourceStream=this.getResourceStream(component);
        if(resourceStream==null){
            throw new FileNotFoundException("Unable to find XSLT resource for "+component.toString());
        }
        try{
            final TransformerFactory tFactory=TransformerFactory.newInstance();
            final Transformer transformer=tFactory.newTransformer(new StreamSource(resourceStream.getInputStream()));
            final StringWriter writer=new StringWriter();
            transformer.transform(new StreamSource(new StringReader(output.toString())),new StreamResult(writer));
            return (CharSequence)writer.getBuffer();
        }
        finally{
            resourceStream.close();
        }
    }
    private IResourceStream getResourceStream(final Component component){
        String filePath=this.xslFile;
        if(filePath==null){
            filePath=component.findParentWithAssociatedMarkup().getClass().getPackage().getName().replace('.','/')+"/"+component.getId();
        }
        final IResourceStream resourceStream=Application.get().getResourceSettings().getResourceStreamLocator().locate((Class<?>)this.getClass(),filePath,component.getStyle(),component.getVariation(),component.getLocale(),"xsl",false);
        return resourceStream;
    }
}
