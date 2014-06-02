package org.apache.wicket.util.tester;

import org.apache.wicket.util.visit.*;
import junit.framework.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.file.*;
import org.apache.wicket.markup.html.form.upload.*;
import org.apache.wicket.protocol.http.mock.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.form.*;
import java.lang.reflect.*;

public class FormTester{
    private final ChoiceSelectorFactory choiceSelectorFactory;
    private boolean closed;
    private final String path;
    private final BaseWicketTester tester;
    private final Form<?> workingForm;
    protected FormTester(final String path,final Form<?> workingForm,final BaseWicketTester wicketTester,final boolean fillBlankString){
        super();
        this.choiceSelectorFactory=new ChoiceSelectorFactory();
        this.closed=false;
        this.path=path;
        this.workingForm=workingForm;
        this.tester=wicketTester;
        workingForm.visitFormComponents((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                if(!formComponent.isVisibleInHierarchy()||!formComponent.isEnabledInHierarchy()){
                    return;
                }
                final String[] values=FormTester.getInputValue(formComponent);
                if(formComponent instanceof AbstractTextComponent&&values.length==0&&fillBlankString){
                    FormTester.this.setFormComponentValue(formComponent,"");
                }
                for(final String value : values){
                    FormTester.this.addFormComponentValue(formComponent,value);
                }
            }
        });
        workingForm.detach();
    }
    public static String[] getInputValue(final FormComponent<?> formComponent){
        if(!formComponent.isVisibleInHierarchy()||!formComponent.isEnabledInHierarchy()){
            return new String[0];
        }
        if(formComponent instanceof AbstractTextComponent){
            return new String[] { getFormComponentValue(formComponent) };
        }
        if(formComponent instanceof DropDownChoice||formComponent instanceof RadioChoice||formComponent instanceof CheckBox){
            return new String[] { getFormComponentValue(formComponent) };
        }
        if(formComponent instanceof ListMultipleChoice){
            return getFormComponentValue(formComponent).split(";");
        }
        if(formComponent instanceof CheckGroup){
            final Collection<?> checkGroupValues=(Collection<?>)formComponent.getDefaultModelObject();
            final List<String> result=(List<String>)new ArrayList();
            formComponent.visitChildren((Class<?>)Check.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
                public void component(final Component component,final IVisit<Void> visit){
                    if(checkGroupValues.contains(component.getDefaultModelObject())){
                        result.add(getFormComponentValue((Check)component));
                    }
                }
            });
            return (String[])result.toArray(new String[result.size()]);
        }
        if(!(formComponent instanceof RadioGroup)){
            return new String[0];
        }
        final Object value=formComponent.getDefaultModelObject();
        String result2=null;
        if(value!=null){
            result2=formComponent.visitChildren((Class<?>)Radio.class,(org.apache.wicket.util.visit.IVisitor<Component,String>)new IVisitor<Component,String>(){
                public void component(final Component component,final IVisit<String> visit){
                    if(value.equals(component.getDefaultModelObject())){
                        visit.stop((Object)getFormComponentValue((Radio)component));
                    }
                    else{
                        visit.dontGoDeeper();
                    }
                }
            });
        }
        if(result2==null){
            return new String[0];
        }
        return new String[] { result2 };
    }
    private static String getFormComponentValue(final FormComponent<?> formComponent){
        final boolean oldEscape=formComponent.getEscapeModelStrings();
        formComponent.setEscapeModelStrings(false);
        final String val=formComponent.getValue();
        formComponent.setEscapeModelStrings(oldEscape);
        return val;
    }
    private static String getFormComponentValue(final Check<?> formComponent){
        final boolean oldEscape=formComponent.getEscapeModelStrings();
        formComponent.setEscapeModelStrings(false);
        final String val=formComponent.getValue();
        formComponent.setEscapeModelStrings(oldEscape);
        return val;
    }
    private static String getFormComponentValue(final Radio<?> formComponent){
        final boolean oldEscape=formComponent.getEscapeModelStrings();
        formComponent.setEscapeModelStrings(false);
        final String val=formComponent.getValue();
        formComponent.setEscapeModelStrings(oldEscape);
        return val;
    }
    public Form<?> getForm(){
        return this.workingForm;
    }
    public String getTextComponentValue(final String id){
        final Component c=this.getForm().get(id);
        if(c instanceof AbstractTextComponent){
            return ((AbstractTextComponent)c).getValue();
        }
        return null;
    }
    public FormTester select(final String formComponentId,final int index){
        this.checkClosed();
        final FormComponent<?> component=(FormComponent<?>)this.workingForm.get(formComponentId);
        final ChoiceSelector choiceSelector=this.choiceSelectorFactory.create(component);
        choiceSelector.doSelect(index);
        try{
            final Method wantOnSelectionChangedNotificationsMethod=component.getClass().getDeclaredMethod("wantOnSelectionChangedNotifications",new Class[0]);
            try{
                wantOnSelectionChangedNotificationsMethod.setAccessible(true);
                final boolean wantOnSelectionChangedNotifications=(boolean)wantOnSelectionChangedNotificationsMethod.invoke(component,new Object[0]);
                if(wantOnSelectionChangedNotifications){
                    this.tester.invokeListener(component,IOnChangeListener.INTERFACE);
                }
            }
            catch(Exception x){
                throw new RuntimeException((Throwable)x);
            }
        }
        catch(NoSuchMethodException ex){
        }
        return this;
    }
    public FormTester selectMultiple(final String formComponentId,final int[] indexes){
        return this.selectMultiple(formComponentId,indexes,false);
    }
    public FormTester selectMultiple(final String formComponentId,final int[] indexes,final boolean replace){
        this.checkClosed();
        if(replace){
            this.setValue(formComponentId,"");
        }
        final ChoiceSelector choiceSelector=this.choiceSelectorFactory.createForMultiple((FormComponent<?>)this.workingForm.get(formComponentId));
        for(final int index : indexes){
            choiceSelector.doSelect(index);
        }
        return this;
    }
    public FormTester setValue(final String formComponentId,final String value){
        final Component component=this.workingForm.get(formComponentId);
        Assert.assertNotNull("Unable to set value. Couldn't find component with name: "+formComponentId,component);
        return this.setValue(component,value);
    }
    public FormTester setValue(final Component formComponent,final String value){
        Args.notNull((Object)formComponent,"formComponent");
        this.checkClosed();
        if(formComponent instanceof IFormSubmittingComponent){
            this.setFormSubmittingComponentValue((IFormSubmittingComponent)formComponent,value);
        }
        else if(formComponent instanceof FormComponent){
            this.setFormComponentValue((FormComponent<?>)formComponent,value);
        }
        else{
            this.fail("Component with id: "+formComponent.getId()+" is not a FormComponent");
        }
        return this;
    }
    public FormTester setValue(final String checkBoxId,final boolean value){
        return this.setValue(checkBoxId,Boolean.toString(value));
    }
    public FormTester setFile(final String formComponentId,final File file,final String contentType){
        this.checkClosed();
        final FormComponent<?> formComponent=(FormComponent<?>)this.workingForm.get(formComponentId);
        if(!(formComponent instanceof FileUploadField)){
            this.fail("'"+formComponentId+"' is not "+"a FileUploadField. You can only attach a file to form "+"component of this type.");
        }
        final MockHttpServletRequest servletRequest=this.tester.getRequest();
        servletRequest.addFile(formComponent.getInputName(),file,contentType);
        return this;
    }
    public FormTester submit(){
        this.checkClosed();
        try{
            this.tester.getLastRenderedPage().getSession().cleanupFeedbackMessages();
            this.tester.getRequest().setUseMultiPartContentType(this.workingForm.isMultiPart());
            this.tester.submitForm(this.path);
        }
        finally{
            this.closed=true;
        }
        return this;
    }
    public FormTester submit(final String buttonComponentId){
        this.setValue(buttonComponentId,"marked");
        return this.submit();
    }
    public FormTester submit(final Component buttonComponent){
        Args.notNull((Object)buttonComponent,"buttonComponent");
        this.setValue(buttonComponent,"marked");
        return this.submit();
    }
    public FormTester submitLink(String path,final boolean pageRelative){
        if(pageRelative){
            this.tester.clickLink(path,false);
        }
        else{
            path=this.path+":"+path;
            this.tester.clickLink(path,false);
        }
        return this;
    }
    private FormTester addFormComponentValue(final FormComponent<?> formComponent,final String value){
        if(this.parameterExist(formComponent)){
            List<StringValue> values=this.tester.getRequest().getPostParameters().getParameterValues(formComponent.getInputName());
            final HashSet<String> all=(HashSet<String>)new HashSet();
            for(final StringValue val : values){
                all.add(val.toString());
            }
            all.add(value);
            values=(List<StringValue>)new ArrayList();
            for(final String val2 : all){
                values.add(StringValue.valueOf(val2));
            }
            this.tester.getRequest().getPostParameters().setParameterValues(formComponent.getInputName(),values);
        }
        else{
            this.setFormComponentValue(formComponent,value);
        }
        return this;
    }
    private void checkClosed(){
        if(this.closed){
            this.fail("'"+this.path+"' already submitted. Note that FormTester "+"is allowed to submit only once");
        }
    }
    private boolean parameterExist(final FormComponent<?> formComponent){
        final String parameter=this.tester.getRequest().getPostParameters().getParameterValue(formComponent.getInputName()).toString();
        return parameter!=null&&parameter.trim().length()>0;
    }
    private void setFormComponentValue(final FormComponent<?> formComponent,final String value){
        this.tester.getRequest().getPostParameters().setParameterValue(formComponent.getInputName(),value);
    }
    private void setFormSubmittingComponentValue(final IFormSubmittingComponent component,final String value){
        this.tester.getRequest().getPostParameters().setParameterValue(component.getInputName(),value);
    }
    private void fail(final String message){
        throw new WicketRuntimeException(message);
    }
    protected abstract class ChoiceSelector{
        private final FormComponent<?> formComponent;
        protected ChoiceSelector(final FormComponent<?> formComponent){
            super();
            this.formComponent=formComponent;
        }
        protected abstract void assignValueToFormComponent(final FormComponent<?> p0,final String p1);
        public String getChoiceValueForIndex(final int index){
            if(this.formComponent instanceof RadioGroup){
                final Radio<?> foundRadio=this.formComponent.visitChildren((Class<?>)Radio.class,(org.apache.wicket.util.visit.IVisitor<Component,Radio<?>>)new SearchOptionByIndexVisitor(index));
                if(foundRadio==null){
                    FormTester.this.fail("RadioGroup "+this.formComponent.getPath()+" does not have index:"+index);
                    return null;
                }
                return foundRadio.getValue();
            }
            else if(this.formComponent instanceof CheckGroup){
                final Check<?> foundCheck=this.formComponent.visitChildren((Class<?>)Check.class,(org.apache.wicket.util.visit.IVisitor<Component,Check<?>>)new SearchOptionByIndexVisitor(index));
                if(foundCheck==null){
                    FormTester.this.fail("CheckGroup "+this.formComponent.getPath()+" does not have index:"+index);
                    return null;
                }
                return foundCheck.getValue();
            }
            else{
                final String idValue=this.selectAbstractChoice(this.formComponent,index);
                if(idValue==null){
                    FormTester.this.fail(this.formComponent.getPath()+" is not a selectable Component.");
                    return null;
                }
                return idValue;
            }
        }
        protected final void doSelect(final int index){
            final String value=this.getChoiceValueForIndex(index);
            this.assignValueToFormComponent(this.formComponent,value);
        }
        private String selectAbstractChoice(final FormComponent<?> formComponent,final int index){
            try{
                final Method getChoicesMethod=formComponent.getClass().getMethod("getChoices",null);
                getChoicesMethod.setAccessible(true);
                final List<Object> choices=(List<Object>)getChoicesMethod.invoke(formComponent,null);
                final Method getChoiceRendererMethod=formComponent.getClass().getMethod("getChoiceRenderer",null);
                getChoiceRendererMethod.setAccessible(true);
                final IChoiceRenderer<Object> choiceRenderer=(IChoiceRenderer<Object>)getChoiceRendererMethod.invoke(formComponent,null);
                return choiceRenderer.getIdValue(choices.get(index),index);
            }
            catch(SecurityException e){
                throw new WicketRuntimeException("unexpect select failure",e);
            }
            catch(NoSuchMethodException e4){
                return null;
            }
            catch(IllegalAccessException e2){
                throw new WicketRuntimeException("unexpect select failure",e2);
            }
            catch(InvocationTargetException e3){
                throw new WicketRuntimeException("unexpect select failure",e3);
            }
        }
        private final class SearchOptionByIndexVisitor implements IVisitor<Component,Component>{
            int count;
            private final int index;
            private SearchOptionByIndexVisitor(final int index){
                super();
                this.count=0;
                this.index=index;
            }
            public void component(final Component component,final IVisit<Component> visit){
                if(this.count==this.index){
                    visit.stop((Object)component);
                }
                else{
                    ++this.count;
                }
            }
        }
    }
    private class ChoiceSelectorFactory{
        final /* synthetic */ FormTester this$0;
        protected ChoiceSelector create(final FormComponent<?> formComponent){
            if(formComponent==null){
                FormTester.this.fail("Trying to select on null component.");
            }
            if(formComponent instanceof RadioGroup||formComponent instanceof DropDownChoice||formComponent instanceof RadioChoice){
                return new SingleChoiceSelector(formComponent);
            }
            if(this.allowMultipleChoice(formComponent)){
                return new MultipleChoiceSelector(formComponent);
            }
            FormTester.this.fail("Selecting on the component:'"+formComponent.getPath()+"' is not supported.");
            return null;
        }
        protected ChoiceSelector createForMultiple(final FormComponent<?> formComponent){
            return new MultipleChoiceSelector(formComponent);
        }
        private boolean allowMultipleChoice(final FormComponent<?> formComponent){
            return formComponent instanceof CheckGroup||formComponent instanceof ListMultipleChoice;
        }
        private final class MultipleChoiceSelector extends ChoiceSelector{
            protected MultipleChoiceSelector(final FormComponent<?> formComponent){
                super(formComponent);
                if(!ChoiceSelectorFactory.this.allowMultipleChoice(formComponent)){
                    ChoiceSelectorFactory.this.this$0.fail("Component:'"+formComponent.getPath()+"' Does not support multiple selection.");
                }
            }
            protected void assignValueToFormComponent(final FormComponent<?> formComponent,final String value){
                FormTester.this.addFormComponentValue(formComponent,value);
            }
        }
        private final class SingleChoiceSelector extends ChoiceSelector{
            protected SingleChoiceSelector(final FormComponent<?> formComponent){
                super(formComponent);
            }
            protected void assignValueToFormComponent(final FormComponent<?> formComponent,final String value){
                FormTester.this.setFormComponentValue(formComponent,value);
            }
        }
    }
}
