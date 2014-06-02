package org.apache.wicket.util.tester;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;

public class DummyPanelPage extends WebPage{
    private static final long serialVersionUID=1L;
    public static final String TEST_PANEL_ID="panel";
    public DummyPanelPage(final ITestPanelSource testPanelSource){
        super();
        this.add(testPanelSource.getTestPanel("panel"));
    }
}
