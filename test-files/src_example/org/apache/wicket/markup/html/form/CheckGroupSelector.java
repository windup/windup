package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;

public class CheckGroupSelector extends AbstractCheckSelector{
    private static final long serialVersionUID=1L;
    private CheckGroup<?> group;
    public CheckGroupSelector(final String id){
        this(id,(CheckGroup<?>)null);
    }
    public CheckGroupSelector(final String id,final CheckGroup<?> group){
        super(id);
        this.group=group;
    }
    private CheckGroup<?> getGroup(){
        CheckGroup<?> group=this.group;
        if(group==null){
            group=this.findParent((Class<CheckGroup<?>>)CheckGroup.class);
            this.group=group;
        }
        return group;
    }
    protected void onBeforeRender(){
        super.onBeforeRender();
        final CheckGroup<?> group=this.getGroup();
        group.getForm().setOutputMarkupId(true);
    }
    public boolean isEnabled(){
        final CheckGroup<?> group=this.getGroup();
        return group==null||(group.isEnableAllowed()&&group.isEnabledInHierarchy());
    }
    protected CharSequence getFindCheckboxesFunction(){
        final CheckGroup<?> group=this.getGroup();
        if(group==null){
            throw new WicketRuntimeException("CheckGroupSelector component ["+this.getPath()+"] cannot find its parent CheckGroup. All CheckGroupSelector components must be a child of or below in the hierarchy of a CheckGroup component.");
        }
        return (CharSequence)String.format("Wicket.CheckboxSelector.findCheckboxesFunction('%s','%s')",new Object[] { group.getForm().getMarkupId(),group.getInputName() });
    }
}
