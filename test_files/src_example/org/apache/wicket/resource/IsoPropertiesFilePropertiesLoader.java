package org.apache.wicket.resource;

import java.util.*;
import java.io.*;
import org.apache.wicket.util.value.*;

public class IsoPropertiesFilePropertiesLoader implements IPropertiesLoader{
    private final String extension;
    public IsoPropertiesFilePropertiesLoader(final String extension){
        super();
        this.extension=extension;
    }
    public final String getFileExtension(){
        return this.extension;
    }
    public Properties loadJavaProperties(final InputStream in) throws IOException{
        final Properties properties=new Properties();
        properties.load(in);
        return properties;
    }
    public ValueMap loadWicketProperties(final InputStream inputStream){
        return null;
    }
}
