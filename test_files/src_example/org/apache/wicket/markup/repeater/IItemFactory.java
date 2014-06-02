package org.apache.wicket.markup.repeater;

import org.apache.wicket.model.*;

public interface IItemFactory<T>{
    Item<T> newItem(int p0,IModel<T> p1);
}
