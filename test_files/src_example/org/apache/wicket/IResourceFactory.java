package org.apache.wicket;

import java.util.*;
import org.apache.wicket.request.resource.*;

public interface IResourceFactory{
    IResource newResource(String p0,Locale p1,String p2,String p3);
}
