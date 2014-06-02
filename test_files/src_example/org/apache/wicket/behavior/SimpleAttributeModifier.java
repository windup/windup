package org.apache.wicket.behavior;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

@Deprecated
public class SimpleAttributeModifier extends Behavior{
    private static final long serialVersionUID=1L;
    private final String attribute;
    private final CharSequence value;
    public SimpleAttributeModifier(final String attribute,final CharSequence value){
        super();
        Args.notNull((Object)attribute,"attribute");
        Args.notNull((Object)value,"value");
        this.attribute=attribute;
        this.value=value;
    }
    public final String getAttribute(){
        return this.attribute;
    }
    public final CharSequence getValue(){
        return this.value;
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
        if(this.isEnabled(component)){
            tag.getAttributes().put((Object)this.attribute,(Object)this.value);
        }
    }
}
