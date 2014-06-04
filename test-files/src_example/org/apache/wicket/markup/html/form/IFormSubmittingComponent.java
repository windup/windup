package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;

public interface IFormSubmittingComponent extends IFormSubmitter{
    Component setDefaultFormProcessing(boolean p0);
    String getInputName();
}
