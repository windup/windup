package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;

public interface ILabelProvider<T>{
    IModel<T> getLabel();
}
