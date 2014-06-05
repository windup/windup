package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import java.io.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.convert.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.validation.*;
import org.slf4j.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.interpolator.*;
import java.util.*;

public abstract class FormComponent<T> extends LabeledWebMarkupContainer implements IFormVisitorParticipant,IFormModelUpdateListener{
    private static final Logger logger;
    public static final String VALUE_SEPARATOR=";";
    private static final String[] EMPTY_STRING_ARRAY;
    private static final short FLAG_REQUIRED=1024;
    private static final String NO_RAW_INPUT="[-NO-RAW-INPUT-]";
    private static final long serialVersionUID=1L;
    protected static final short FLAG_CONVERT_EMPTY_INPUT_STRING_TO_NULL=256;
    private transient T convertedInput;
    private String rawInput;
    private String typeName;
    public static final <R> R visitFormComponentsPostOrder(final Component component,final IVisitor<? extends FormComponent<?>,R> visitor){
        return (R)Visits.visitPostOrder((Object)component,(IVisitor)visitor,(IVisitFilter)new IVisitFilter(){
            public boolean visitChildren(final Object object){
                return !(object instanceof IFormVisitorParticipant)||((IFormVisitorParticipant)object).processChildren();
            }
            public boolean visitObject(final Object object){
                return object instanceof FormComponent;
            }
        });
    }
    public static final <R> R visitComponentsPostOrder(final Component component,final IVisitor<Component,R> visitor){
        Args.notNull((Object)visitor,"visitor");
        return (R)Visits.visitPostOrder((Object)component,(IVisitor)visitor,(IVisitFilter)new IVisitFilter(){
            public boolean visitObject(final Object object){
                return true;
            }
            public boolean visitChildren(final Object object){
                return !(object instanceof IFormVisitorParticipant)||((IFormVisitorParticipant)object).processChildren();
            }
        });
    }
    public FormComponent(final String id){
        this(id,null);
    }
    public FormComponent(final String id,final IModel<T> model){
        super(id,model);
        this.rawInput="[-NO-RAW-INPUT-]";
        this.setVersioned(false);
    }
    public final String getDefaultLabel(){
        return this.getDefaultLabel(this.getId());
    }
    public final String getDefaultLabel(final String defaultValue){
        return this.getLocalizer().getString(this.getId(),this.getParent(),defaultValue);
    }
    public final FormComponent<T> add(final IValidator<? super T> validator){
        Args.notNull((Object)validator,"validator");
        if(validator instanceof Behavior){
            this.add((Behavior)validator);
        }
        else{
            this.add(new Behavior[] { new ValidatorAdapter<Object>(validator) });
        }
        return this;
    }
    public final FormComponent<T> remove(final IValidator<? super T> validator){
        Args.notNull((Object)validator,"validator");
        Behavior match=null;
        for(final Behavior behavior : this.getBehaviors()){
            if(behavior.equals(validator)){
                match=behavior;
                break;
            }
            if(behavior instanceof ValidatorAdapter&&((ValidatorAdapter)behavior).getValidator().equals(validator)){
                match=behavior;
                break;
            }
        }
        if(match!=null){
            this.remove(match);
            return this;
        }
        throw new IllegalStateException("Tried to remove validator that was not previously added. Make sure your validator's equals() implementation is sufficient");
    }
    public final FormComponent<T> add(final IValidator<? super T>... validators){
        Args.notNull((Object)validators,"validators");
        for(final IValidator<? super T> validator : validators){
            this.add(validator);
        }
        return this;
    }
    public boolean checkRequired(){
        if(this.isRequired()){
            final String input=this.getInput();
            return (input==null&&!this.isInputNullable()&&!this.isEnabledInHierarchy())||!Strings.isEmpty((CharSequence)input);
        }
        return true;
    }
    public final void clearInput(){
        this.rawInput="[-NO-RAW-INPUT-]";
    }
    public void error(final IValidationError error){
        Args.notNull((Object)error,"error");
        final MessageSource source=new MessageSource();
        String message=error.getErrorMessage(source);
        if(message==null){
            final StringBuilder buffer=new StringBuilder();
            buffer.append("Could not locate error message for component: ");
            buffer.append(Classes.simpleName(this.getClass()));
            buffer.append("@");
            buffer.append(this.getPageRelativePath());
            buffer.append(" and error: ");
            buffer.append(error.toString());
            buffer.append(". Tried keys: ");
            final Iterator<String> keys=(Iterator<String>)source.triedKeys.iterator();
            while(keys.hasNext()){
                buffer.append((String)keys.next());
                if(keys.hasNext()){
                    buffer.append(", ");
                }
            }
            buffer.append(".");
            message=buffer.toString();
            FormComponent.logger.warn(message);
        }
        this.error((Serializable)new ValidationErrorFeedback(error,message));
    }
    public final T getConvertedInput(){
        return this.convertedInput;
    }
    public final void setConvertedInput(final T convertedInput){
        this.convertedInput=convertedInput;
    }
    public Form<?> getForm(){
        final Form<?> form=Form.findForm(this);
        if(form==null){
            throw new WicketRuntimeException("Could not find Form parent for "+this);
        }
        return form;
    }
    public String getInput(){
        final String[] input=this.getInputAsArray();
        if(input==null||input.length==0){
            return null;
        }
        return this.trim(input[0]);
    }
    public String[] getInputAsArray(){
        final List<StringValue> list=(List<StringValue>)this.getRequest().getRequestParameters().getParameterValues(this.getInputName());
        String[] values=null;
        if(list!=null){
            values=new String[list.size()];
            for(int i=0;i<list.size();++i){
                values[i]=((StringValue)list.get(i)).toString();
            }
        }
        if(!this.isInputNullable()&&values!=null&&values.length==1&&values[0]==null){
            return FormComponent.EMPTY_STRING_ARRAY;
        }
        return values;
    }
    public String getInputName(){
        final String inputName=Form.getRootFormRelativeId(this);
        final Form<?> form=this.findParent((Class<Form<?>>)Form.class);
        if(form!=null){
            return form.getInputNamePrefix()+inputName;
        }
        return inputName;
    }
    public final String getRawInput(){
        return "[-NO-RAW-INPUT-]".equals(this.rawInput)?null:this.rawInput;
    }
    public final Class<T> getType(){
        return (this.typeName==null)?null:WicketObjects.resolveClass(this.typeName);
    }
    public String getValidatorKeyPrefix(){
        final Form<?> form=this.findParent((Class<Form<?>>)Form.class);
        if(form!=null){
            return this.getForm().getValidatorKeyPrefix();
        }
        return null;
    }
    public final List<IValidator<? super T>> getValidators(){
        final List<IValidator<? super T>> list=(List<IValidator<? super T>>)new ArrayList();
        for(final Behavior behavior : this.getBehaviors()){
            if(behavior instanceof IValidator){
                list.add(behavior);
            }
        }
        return (List<IValidator<? super T>>)Collections.unmodifiableList(list);
    }
    public final String getValue(){
        if("[-NO-RAW-INPUT-]".equals(this.rawInput)){
            return this.getModelValue();
        }
        if(this.getEscapeModelStrings()&&this.rawInput!=null){
            return Strings.escapeMarkup((CharSequence)this.rawInput).toString();
        }
        return this.rawInput;
    }
    public final boolean hasRawInput(){
        return !"[-NO-RAW-INPUT-]".equals(this.rawInput);
    }
    public final void inputChanged(){
        if(this.isVisibleInHierarchy()&&this.isEnabledInHierarchy()){
            final String[] input=this.getInputAsArray();
            if(input!=null&&input.length>0&&input[0]!=null){
                this.rawInput=StringList.valueOf(input).join(";");
            }
            else if(this.isInputNullable()){
                this.rawInput=null;
            }
            else{
                this.rawInput="[-NO-RAW-INPUT-]";
            }
        }
    }
    public final void invalid(){
        this.onInvalid();
    }
    public boolean isInputNullable(){
        return true;
    }
    public boolean isMultiPart(){
        return false;
    }
    public boolean isRequired(){
        return this.getFlag(1024);
    }
    public final boolean isValid(){
        class IsValidVisitor implements IVisitor<FormComponent<?>,Boolean>{
            public void component(final FormComponent<?> formComponent,final IVisit<Boolean> visit){
                if(formComponent.hasErrorMessage()){
                    visit.stop((Object)Boolean.FALSE);
                }
            }
        }
        final IsValidVisitor tmp=new IsValidVisitor();
        final Object result=visitFormComponentsPostOrder(this,(org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)tmp);
        return Boolean.FALSE!=result;
    }
    public boolean processChildren(){
        return true;
    }
    public final void processInput(){
        this.inputChanged();
        this.validate();
        if(this.hasErrorMessage()){
            this.invalid();
        }
        else{
            this.valid();
            this.updateModel();
        }
    }
    public FormComponent<T> setLabel(final IModel<String> labelModel){
        this.setLabelInternal(labelModel);
        return this;
    }
    public void setModelValue(final String[] value){
        this.convertedInput=this.convertValue(value);
        this.updateModel();
    }
    public final FormComponent<T> setRequired(final boolean required){
        if(!required&&this.getType()!=null&&this.getType().isPrimitive()){
            throw new WicketRuntimeException("FormComponent can't be required when the type is primitive class: "+this);
        }
        if(required!=this.isRequired()){
            this.addStateChange();
        }
        this.setFlag(1024,required);
        return this;
    }
    public FormComponent<T> setType(final Class<?> type){
        this.typeName=((type==null)?null:type.getName());
        if(type!=null&&type.isPrimitive()){
            this.setRequired(true);
        }
        return this;
    }
    public void updateModel(){
        this.setModelObject(this.getConvertedInput());
    }
    public final void valid(){
        this.clearInput();
        this.onValid();
    }
    public void validate(){
        this.validateRequired();
        if(this.isValid()){
            this.convertInput();
            if(this.isValid()){
                if(this.isRequired()&&this.getConvertedInput()==null&&this.isInputNullable()){
                    this.reportRequiredError();
                }
                else{
                    this.validateValidators();
                }
            }
        }
    }
    protected void convertInput(){
        if(this.typeName==null){
            try{
                this.convertedInput=this.convertValue(this.getInputAsArray());
            }
            catch(ConversionException e){
                final ValidationError error=new ValidationError();
                if(e.getResourceKey()!=null){
                    error.addMessageKey(e.getResourceKey());
                }
                if(e.getTargetType()!=null){
                    error.addMessageKey("ConversionError."+Classes.simpleName(e.getTargetType()));
                }
                error.addMessageKey("ConversionError");
                this.reportValidationError(e,error);
            }
        }
        else{
            final IConverter<T> converter=this.getConverter(this.getType());
            try{
                this.convertedInput=(T)converter.convertToObject(this.getInput(),this.getLocale());
            }
            catch(ConversionException e2){
                final ValidationError error2=new ValidationError();
                if(e2.getResourceKey()!=null){
                    error2.addMessageKey(e2.getResourceKey());
                }
                final String simpleName=Classes.simpleName((Class)this.getType());
                error2.addMessageKey("IConverter."+simpleName);
                error2.addMessageKey("IConverter");
                error2.setVariable("type",simpleName);
                this.reportValidationError(e2,error2);
            }
        }
    }
    private void reportValidationError(final ConversionException e,final ValidationError error){
        final Locale locale=e.getLocale();
        if(locale!=null){
            error.setVariable("locale",locale);
        }
        error.setVariable("exception",e);
        final Format format=e.getFormat();
        if(format instanceof SimpleDateFormat){
            error.setVariable("format",((SimpleDateFormat)format).toLocalizedPattern());
        }
        final Map<String,Object> variables=(Map<String,Object>)e.getVariables();
        if(variables!=null){
            error.getVariables().putAll(variables);
        }
        this.error(error);
    }
    protected T convertValue(final String[] value) throws ConversionException{
        return (T)((value!=null&&value.length>0&&value[0]!=null)?this.trim(value[0]):null);
    }
    protected String getModelValue(){
        return this.getDefaultModelObjectAsString();
    }
    protected final int inputAsInt(){
        final String string=this.getInput();
        try{
            return Integer.parseInt(string);
        }
        catch(NumberFormatException e){
            throw new IllegalArgumentException(this.exceptionMessage("Internal error.  Request string '"+string+"' not a valid integer"));
        }
    }
    protected final int inputAsInt(final int defaultValue){
        final String string=this.getInput();
        if(string!=null){
            try{
                return Integer.parseInt(string);
            }
            catch(NumberFormatException e){
                throw new IllegalArgumentException(this.exceptionMessage("Request string '"+string+"' is not a valid integer"));
            }
        }
        return defaultValue;
    }
    protected final int[] inputAsIntArray(){
        final String[] strings=this.getInputAsArray();
        if(strings!=null){
            final int[] ints=new int[strings.length];
            for(int i=0;i<strings.length;++i){
                ints[i]=Integer.parseInt(strings[i]);
            }
            return ints;
        }
        return null;
    }
    protected void internalOnModelChanged(){
        this.valid();
    }
    protected void onComponentTag(final ComponentTag tag){
        tag.put("name",(CharSequence)this.getInputName());
        if(!this.isEnabledInHierarchy()){
            this.onDisabled(tag);
        }
        super.onComponentTag(tag);
    }
    protected void onDetach(){
        super.onDetach();
        this.convertedInput=null;
    }
    protected void onDisabled(final ComponentTag tag){
        tag.put("disabled",(CharSequence)"disabled");
    }
    protected void onInvalid(){
    }
    protected void onValid(){
    }
    protected boolean shouldTrimInput(){
        return true;
    }
    protected final String trim(final String string){
        String trimmed=string;
        if(trimmed!=null&&this.shouldTrimInput()){
            trimmed=trimmed.trim();
        }
        return trimmed;
    }
    protected final void validateRequired(){
        if(!this.checkRequired()){
            this.reportRequiredError();
        }
    }
    protected void reportRequiredError(){
        this.error(new ValidationError().addMessageKey("Required"));
    }
    protected final void validateValidators(){
        final IValidatable<T> validatable=this.newValidatable();
        final boolean isNull=this.getConvertedInput()==null;
        IValidator<T> validator=null;
        try{
            for(final Behavior behavior : this.getBehaviors()){
                validator=null;
                if(behavior instanceof ValidatorAdapter){
                    validator=((ValidatorAdapter)behavior).getValidator();
                }
                else if(behavior instanceof IValidator){
                    validator=(IValidator<T>)behavior;
                }
                if(validator!=null){
                    if(!isNull||validator instanceof INullAcceptingValidator){
                        validator.validate(validatable);
                    }
                    if(!this.isValid()){
                        break;
                    }
                    continue;
                }
            }
        }
        catch(Exception e){
            throw new WicketRuntimeException("Exception '"+e.getMessage()+"' occurred during validation "+validator.getClass().getName()+" on component "+this.getPath(),e);
        }
    }
    public final IValidatable<T> newValidatable(){
        return new ValidatableAdapter();
    }
    public final IModel<T> getModel(){
        return (IModel<T>)this.getDefaultModel();
    }
    public final void setModel(final IModel<T> model){
        this.setDefaultModel(model);
    }
    public final T getModelObject(){
        return (T)this.getDefaultModelObject();
    }
    public final void setModelObject(final T object){
        this.setDefaultModelObject(object);
    }
    protected static <S> void updateCollectionModel(final FormComponent<Collection<S>> formComponent){
        final Collection<S> convertedInput=formComponent.getConvertedInput();
        Collection<S> collection=formComponent.getModelObject();
        if(collection==null){
            collection=(Collection<S>)new ArrayList(convertedInput);
            formComponent.setDefaultModelObject(collection);
        }
        else{
            formComponent.modelChanging();
            collection.clear();
            if(convertedInput!=null){
                collection.addAll(convertedInput);
            }
            formComponent.modelChanged();
            try{
                formComponent.getModel().setObject(collection);
            }
            catch(Exception e){
                FormComponent.logger.info("no setter for the property attached to "+formComponent);
            }
        }
    }
    static{
        logger=LoggerFactory.getLogger(FormComponent.class);
        EMPTY_STRING_ARRAY=new String[] { "" };
    }
    private class MessageSource implements IErrorMessageSource{
        private final Set<String> triedKeys;
        private MessageSource(){
            super();
            this.triedKeys=(Set<String>)new LinkedHashSet();
        }
        public String getMessage(final String key){
            final FormComponent<T> formComponent=FormComponent.this;
            final Localizer localizer=formComponent.getLocalizer();
            final String prefix=formComponent.getValidatorKeyPrefix();
            String message=null;
            String resource=FormComponent.this.getId()+"."+this.prefix(prefix,key);
            message=this.getString(localizer,resource,formComponent);
            if(Strings.isEmpty((CharSequence)message)&&Strings.isEmpty((CharSequence)prefix)){
                resource=FormComponent.this.getId()+"."+key;
                message=this.getString(localizer,resource,formComponent);
            }
            if(Strings.isEmpty((CharSequence)message)){
                resource=this.prefix(prefix,key);
                message=this.getString(localizer,resource,formComponent);
            }
            if(Strings.isEmpty((CharSequence)message)){
                message=this.getString(localizer,key,formComponent);
            }
            if(Strings.isEmpty((CharSequence)message)){
                message=null;
            }
            return message;
        }
        private String prefix(final String prefix,final String key){
            if(!Strings.isEmpty((CharSequence)prefix)){
                return prefix+"."+key;
            }
            return key;
        }
        private String getString(final Localizer localizer,final String key,final Component component){
            this.triedKeys.add(key);
            return localizer.getString(key,component,"");
        }
        public String substitute(final String string,final Map<String,Object> vars) throws IllegalStateException{
            return new MapVariableInterpolator(string,(Map)this.addDefaultVars(vars),Application.get().getResourceSettings().getThrowExceptionOnMissingResource()).toString();
        }
        private Map<String,Object> addDefaultVars(final Map<String,Object> params){
            HashMap<String,Object> fullParams;
            if(params==null){
                fullParams=(HashMap<String,Object>)new HashMap(6);
            }
            else{
                fullParams=(HashMap<String,Object>)new HashMap(params.size()+6);
                fullParams.putAll(params);
            }
            if(!fullParams.containsKey("input")){
                fullParams.put("input",FormComponent.this.getInput());
            }
            if(!fullParams.containsKey("name")){
                fullParams.put("name",FormComponent.this.getId());
            }
            if(!fullParams.containsKey("label")){
                fullParams.put("label",this.getLabel());
            }
            return (Map<String,Object>)fullParams;
        }
        private String getLabel(){
            final FormComponent<T> fc=FormComponent.this;
            String label=null;
            if(fc.getLabel()!=null){
                label=fc.getLabel().getObject();
            }
            if(label==null){
                label=fc.getDefaultLabel();
            }
            return label;
        }
    }
    private class ValidatableAdapter implements IValidatable<T>{
        public void error(final IValidationError error){
            FormComponent.this.error(error);
        }
        public T getValue(){
            return FormComponent.this.getConvertedInput();
        }
        public boolean isValid(){
            return FormComponent.this.isValid();
        }
        public IModel<T> getModel(){
            return FormComponent.this.getModel();
        }
    }
}
