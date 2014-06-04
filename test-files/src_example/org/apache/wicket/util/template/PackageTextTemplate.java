package org.apache.wicket.util.template;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.util.resource.locator.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.util.resource.*;
import java.util.*;
import org.apache.wicket.util.string.interpolator.*;
import org.slf4j.*;
import java.io.*;

public class PackageTextTemplate extends TextTemplate{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public static final String DEFAULT_CONTENT_TYPE="text";
    public static final String DEFAULT_ENCODING;
    private final StringBuilder buffer;
    public PackageTextTemplate(final Class<?> clazz,final String fileName){
        this(clazz,fileName,"text");
    }
    public PackageTextTemplate(final Class<?> clazz,final String fileName,final String contentType){
        this(clazz,fileName,contentType,PackageTextTemplate.DEFAULT_ENCODING);
    }
    public PackageTextTemplate(final Class<?> clazz,final String fileName,final String contentType,final String encoding){
        this(clazz,fileName,null,null,null,contentType,encoding);
    }
    public PackageTextTemplate(final Class<?> clazz,final String fileName,final String style,final String variation,final Locale locale,final String contentType,final String encoding){
        super(contentType);
        this.buffer=new StringBuilder();
        final String path=Packages.absolutePath((Class)clazz,fileName);
        final Application app=Application.get();
        IResourceStream stream=app.getResourceSettings().getResourceStreamLocator().locate(clazz,path,style,variation,locale,null,false);
        if(stream==null){
            stream=new ResourceStreamLocator().locate(clazz,path,style,variation,locale,null,false);
        }
        if(stream==null){
            throw new IllegalArgumentException("resource "+fileName+" not found for scope "+clazz+" (path = "+path+")");
        }
        this.setLastModified(stream.lastModifiedTime());
        try{
            if(encoding!=null){
                this.buffer.append(Streams.readString(stream.getInputStream(),(CharSequence)encoding));
            }
            else{
                this.buffer.append(Streams.readString(stream.getInputStream()));
            }
        }
        catch(IOException e){
            throw new RuntimeException((Throwable)e);
        }
        catch(ResourceStreamNotFoundException e2){
            throw new RuntimeException((Throwable)e2);
        }
        finally{
            try{
                stream.close();
            }
            catch(IOException e3){
                PackageTextTemplate.log.error(e3.getMessage(),e3);
            }
        }
    }
    public String getString(){
        return this.buffer.toString();
    }
    public final TextTemplate interpolate(final Map<String,?> variables){
        if(variables!=null){
            final String result=new MapVariableInterpolator(this.buffer.toString(),(Map)variables).toString();
            this.buffer.delete(0,this.buffer.length());
            this.buffer.append(result);
        }
        return this;
    }
    static{
        log=LoggerFactory.getLogger(PackageTextTemplate.class);
        DEFAULT_ENCODING=null;
    }
}
