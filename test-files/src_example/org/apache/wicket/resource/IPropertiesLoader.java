package org.apache.wicket.resource;

import org.apache.wicket.util.value.*;
import java.util.*;
import java.io.*;

public interface IPropertiesLoader{
    ValueMap loadWicketProperties(InputStream p0);
    Properties loadJavaProperties(InputStream p0) throws IOException;
    String getFileExtension();
}
