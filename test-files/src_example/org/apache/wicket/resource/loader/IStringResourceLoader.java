package org.apache.wicket.resource.loader;

import java.util.*;
import org.apache.wicket.*;

public interface IStringResourceLoader{
    String loadStringResource(Class<?> p0,String p1,Locale p2,String p3,String p4);
    String loadStringResource(Component p0,String p1,Locale p2,String p3,String p4);
}
