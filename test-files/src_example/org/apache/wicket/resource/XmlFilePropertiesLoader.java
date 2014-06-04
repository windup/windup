package org.apache.wicket.resource;

import java.util.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import org.apache.wicket.util.value.*;

public class XmlFilePropertiesLoader implements IPropertiesLoader{
    private final String fileExtension;
    public XmlFilePropertiesLoader(final String fileExtension){
        super();
        this.fileExtension=fileExtension;
    }
    public final String getFileExtension(){
        return this.fileExtension;
    }
    public Properties loadJavaProperties(final InputStream in) throws IOException{
        final Properties properties=new Properties();
        Streams.loadFromXml(properties,in);
        return properties;
    }
    public ValueMap loadWicketProperties(final InputStream inputStream){
        return null;
    }
}
