package org.apache.wicket.markup.loader;

import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import java.io.*;
import org.apache.wicket.util.resource.*;

public class SimpleMarkupLoader implements IMarkupLoader{
    public final Markup loadMarkup(final MarkupContainer container,final MarkupResourceStream markupResourceStream,final IMarkupLoader baseLoader,final boolean enforceReload) throws IOException,ResourceStreamNotFoundException{
        return MarkupFactory.get().newMarkupParser(markupResourceStream).parse();
    }
}
