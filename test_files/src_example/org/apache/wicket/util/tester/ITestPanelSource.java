package org.apache.wicket.util.tester;

import org.apache.wicket.*;
import org.apache.wicket.markup.html.panel.*;

@Deprecated
public interface ITestPanelSource extends IClusterable{
    Panel getTestPanel(String p0);
}
