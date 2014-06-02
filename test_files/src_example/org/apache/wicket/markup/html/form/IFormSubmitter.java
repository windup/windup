package org.apache.wicket.markup.html.form;

public interface IFormSubmitter{
    Form<?> getForm();
    boolean getDefaultFormProcessing();
    void onSubmit();
    void onError();
}
