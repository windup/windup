package org.apache.wicket.markup.repeater;

import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.model.*;

public interface IItemReuseStrategy extends IClusterable{
     <T> Iterator<Item<T>> getItems(IItemFactory<T> p0,Iterator<IModel<T>> p1,Iterator<Item<T>> p2);
}
