package org.apache.wicket.markup.html.link;

import org.apache.wicket.*;

public interface IPageLink extends IClusterable{
    Page getPage();
    Class<? extends Page> getPageIdentity();
}
