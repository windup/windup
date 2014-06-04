package org.apache.wicket.markup.html.form;

public class CheckboxMultipleChoiceSelector extends AbstractCheckSelector{
    private static final long serialVersionUID=1L;
    private final CheckBoxMultipleChoice<?> choiceComponent;
    public CheckboxMultipleChoiceSelector(final String id,final CheckBoxMultipleChoice<?> choiceComponent){
        super(id);
        (this.choiceComponent=choiceComponent).setOutputMarkupId(true);
    }
    protected CharSequence getFindCheckboxesFunction(){
        return (CharSequence)String.format("Wicket.CheckboxSelector.findCheckboxesFunction('%s', '%s')",new Object[] { this.choiceComponent.getMarkupId(),this.choiceComponent.getInputName() });
    }
}
