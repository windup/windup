package org.apache.wicket.markup.loader;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import java.io.*;
import org.apache.wicket.util.resource.*;

public interface IMarkupLoader{
    Markup loadMarkup(MarkupContainer p0,MarkupResourceStream p1,IMarkupLoader p2,boolean p3) throws IOException,ResourceStreamNotFoundException;
}
