package org.apache.wicket.markup.parser;

import java.text.*;
import org.apache.wicket.markup.*;

public interface IMarkupFilter{
    IMarkupFilter getNextFilter();
    void setNextFilter(IMarkupFilter p0);
    MarkupElement nextElement() throws ParseException;
    void postProcess(Markup p0);
}
