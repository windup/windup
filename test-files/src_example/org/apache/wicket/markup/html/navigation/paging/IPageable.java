package org.apache.wicket.markup.html.navigation.paging;

import org.apache.wicket.*;

public interface IPageable extends IClusterable{
    int getCurrentPage();
    void setCurrentPage(int p0);
    int getPageCount();
}
