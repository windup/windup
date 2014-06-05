package org.apache.wicket.protocol.http;

import java.util.*;
import org.apache.wicket.util.upload.*;

public interface IMultipartWebRequest{
    Map<String,List<FileItem>> getFiles();
    List<FileItem> getFile(String p0);
}
