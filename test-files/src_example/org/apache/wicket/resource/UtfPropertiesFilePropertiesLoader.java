package org.apache.wicket.resource;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import org.apache.wicket.util.value.*;

public class UtfPropertiesFilePropertiesLoader implements IPropertiesLoader{
    private final String fileExtension;
    private final String encoding;
    private Method load;
    public UtfPropertiesFilePropertiesLoader(final String fileExtension,final String encoding){
        super();
        this.fileExtension=fileExtension;
        this.encoding=encoding;
        try{
            this.load=Properties.class.getMethod("load",new Class[] { Reader.class });
        }
        catch(NoSuchMethodException ex){
            this.load=null;
        }
    }
    public final String getFileExtension(){
        return this.fileExtension;
    }
    public Properties loadJavaProperties(final InputStream in) throws IOException{
        if(this.load==null){
            return null;
        }
        Properties properties=new Properties();
        final Reader reader=new InputStreamReader(in,this.encoding);
        try{
            this.load.invoke(properties,new Object[] { reader });
        }
        catch(IllegalArgumentException ex){
            properties=null;
        }
        catch(IllegalAccessException ex2){
            properties=null;
        }
        catch(InvocationTargetException ex3){
            properties=null;
        }
        return properties;
    }
    public ValueMap loadWicketProperties(final InputStream inputStream){
        return null;
    }
}
