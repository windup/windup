package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;
import java.util.*;
import java.io.*;

public class CheckBoxSelector extends AbstractCheckSelector{
    private static final long serialVersionUID=1L;
    private final String checkBoxIdArrayLiteral;
    public CheckBoxSelector(final String id,final CheckBox... boxes){
        super(id);
        this.checkBoxIdArrayLiteral=this.buildMarkupIdJSArrayLiteral((Iterable<? extends Component>)Arrays.asList(boxes));
    }
    protected CharSequence getFindCheckboxesFunction(){
        return (CharSequence)String.format("Wicket.CheckboxSelector.getCheckboxesFunction(%s)",new Object[] { this.checkBoxIdArrayLiteral });
    }
    private String buildMarkupIdJSArrayLiteral(final Iterable<? extends Component> components){
        final StringBuilder buf=new StringBuilder();
        buf.append("[");
        if(components.iterator().hasNext()){
            for(final Component component : components){
                component.setOutputMarkupId(true);
                buf.append("'").append(component.getMarkupId()).append("', ");
            }
            buf.delete(buf.length()-2,buf.length());
        }
        buf.append("]");
        return buf.toString();
    }
}
