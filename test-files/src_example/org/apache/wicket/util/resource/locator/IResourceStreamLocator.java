package org.apache.wicket.util.resource.locator;

import org.apache.wicket.util.resource.*;
import java.util.*;

public interface IResourceStreamLocator{
    IResourceStream locate(Class<?> p0,String p1);
    IResourceStream locate(Class<?> p0,String p1,String p2,String p3,Locale p4,String p5,boolean p6);
    ResourceNameIterator newResourceNameIterator(String p0,Locale p1,String p2,String p3,String p4,boolean p5);
}
