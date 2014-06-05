package org.apache.wicket.application;

import java.util.*;
import java.net.*;

public interface IClassResolver{
    Class<?> resolveClass(String p0) throws ClassNotFoundException;
    Iterator<URL> getResources(String p0);
}
