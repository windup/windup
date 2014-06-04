package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;

public interface IChoiceRenderer<T> extends IClusterable{
    Object getDisplayValue(T p0);
    String getIdValue(T p0,int p1);
}
