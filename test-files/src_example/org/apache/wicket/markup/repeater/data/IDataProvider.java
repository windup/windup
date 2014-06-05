package org.apache.wicket.markup.repeater.data;

import java.util.*;
import org.apache.wicket.model.*;

public interface IDataProvider<T> extends IDetachable{
    Iterator<? extends T> iterator(int p0,int p1);
    int size();
    IModel<T> model(T p0);
}
