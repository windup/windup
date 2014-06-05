package org.apache.wicket.markup.html;

import org.apache.wicket.*;

public interface IComponentAwareHeaderContributor extends IClusterable{
    void renderHead(Component p0,IHeaderResponse p1);
}
