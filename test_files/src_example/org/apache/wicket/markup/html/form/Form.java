package org.apache.wicket.markup.html.form;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.html.form.validation.*;
import org.apache.wicket.util.string.interpolator.*;
import java.io.*;
import org.apache.wicket.util.value.*;
import javax.servlet.http.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.protocol.http.servlet.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.util.upload.*;
import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class Form<T> extends WebMarkupContainer implements IFormSubmitListener{
    private static final String HIDDEN_DIV_START="<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">";
    public static final String METHOD_GET="get";
    public static final String METHOD_POST="post";
    private static final short FLAG_SUBMITTED=256;
    private static final Logger log;
    private static final long serialVersionUID=1L;
    private static final String UPLOAD_FAILED_RESOURCE_KEY="uploadFailed";
    private static final String UPLOAD_TOO_LARGE_RESOURCE_KEY="uploadTooLarge";
    private IFormSubmittingComponent defaultSubmittingComponent;
    private Bytes maxSize;
    private short multiPart;
    private static final short MULTIPART_HARD=1;
    private static final short MULTIPART_HINT=2;
    public Form(final String id){
        this(id,null);
    }
    public Form(final String id,final IModel<T> model){
        super(id,model);
        this.maxSize=null;
        this.multiPart=0;
        this.setOutputMarkupId(true);
    }
    public void add(final IFormValidator validator){
        Args.notNull((Object)validator,"validator");
        if(validator instanceof Behavior){
            this.add((Behavior)validator);
        }
        else{
            this.add(new FormValidatorAdapter(validator));
        }
    }
    public void remove(final IFormValidator validator){
        Args.notNull((Object)validator,"validator");
        Behavior match=null;
        for(final Behavior behavior : this.getBehaviors()){
            if(behavior.equals(validator)){
                match=behavior;
                break;
            }
            if(behavior instanceof FormValidatorAdapter&&((FormValidatorAdapter)behavior).getValidator().equals(validator)){
                match=behavior;
                break;
            }
        }
        if(match!=null){
            this.remove(match);
            return;
        }
        throw new IllegalStateException("Tried to remove form validator that was not previously added. Make sure your validator's equals() implementation is sufficient");
    }
    public final void clearInput(){
        this.visitFormComponentsPostOrder((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                if(formComponent.isVisibleInHierarchy()){
                    formComponent.clearInput();
                }
            }
        });
    }
    public final void error(final String error,final Map<String,Object> args){
        this.error(new MapVariableInterpolator(error,(Map)args).toString());
    }
    public final IFormSubmitter findSubmittingButton(){
        final IFormSubmittingComponent submittingComponent=this.getPage().visitChildren((Class<?>)IFormSubmittingComponent.class,(org.apache.wicket.util.visit.IVisitor<Component,IFormSubmittingComponent>)new IVisitor<Component,IFormSubmittingComponent>(){
            public void component(final Component component,final IVisit<IFormSubmittingComponent> visit){
                final IFormSubmittingComponent submittingComponent=(IFormSubmittingComponent)component;
                final Form<?> form=submittingComponent.getForm();
                if(form!=null&&form.getRootForm()==Form.this){
                    final String name=submittingComponent.getInputName();
                    final IRequestParameters parameters=Form.this.getRequest().getRequestParameters();
                    if(!parameters.getParameterValue(name).isNull()||!parameters.getParameterValue(name+".x").isNull()){
                        if(!component.isVisibleInHierarchy()){
                            throw new WicketRuntimeException("Submit Button "+submittingComponent.getInputName()+" (path="+component.getPageRelativePath()+") is not visible");
                        }
                        if(!component.isEnabledInHierarchy()){
                            throw new WicketRuntimeException("Submit Button "+submittingComponent.getInputName()+" (path="+component.getPageRelativePath()+") is not enabled");
                        }
                        visit.stop((Object)submittingComponent);
                    }
                }
            }
        });
        return submittingComponent;
    }
    public final IFormSubmittingComponent getDefaultButton(){
        if(this.isRootForm()){
            return this.defaultSubmittingComponent;
        }
        return this.getRootForm().getDefaultButton();
    }
    public final Collection<IFormValidator> getFormValidators(){
        final List<IFormValidator> validators=(List<IFormValidator>)new ArrayList();
        for(final Behavior behavior : this.getBehaviors()){
            if(behavior instanceof IFormValidator){
                validators.add(behavior);
            }
        }
        return (Collection<IFormValidator>)Collections.unmodifiableCollection(validators);
    }
    public final CharSequence getJsForInterfaceUrl(CharSequence url){
        final UrlRenderer renderer=this.getRequestCycle().getUrlRenderer();
        final Url oldBase=renderer.getBaseUrl();
        try{
            final Url action=Url.parse(this.getActionUrl().toString());
            renderer.setBaseUrl(action);
            url=(CharSequence)renderer.renderUrl(Url.parse(url.toString()));
        }
        finally{
            renderer.setBaseUrl(oldBase);
        }
        final Form<?> root=this.getRootForm();
        return (CharSequence)new AppendingStringBuffer((CharSequence)"document.getElementById('").append(root.getHiddenFieldId()).append("').value='").append((Object)url).append("';document.getElementById('").append(root.getMarkupId()).append("').submit();");
    }
    public final Bytes getMaxSize(){
        final Bytes[] maxSize= { this.maxSize };
        if(maxSize[0]==null){
            this.visitChildren((Class<?>)Form.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Form<?>,Bytes>(){
                public void component(final Form<?> component,final IVisit<Bytes> visit){
                    maxSize[0]=(Bytes)LongValue.maxNullSafe((LongValue)maxSize[0],(LongValue)component.maxSize);
                }
            });
        }
        if(maxSize[0]==null){
            return this.getApplication().getApplicationSettings().getDefaultMaximumUploadSize();
        }
        return maxSize[0];
    }
    public Form<?> getRootForm(){
        Form<?> parent=this;
        Form<?> form;
        do{
            form=parent;
            parent=form.findParent((Class<Form<?>>)Form.class);
        } while(parent!=null);
        return form;
    }
    public String getValidatorKeyPrefix(){
        return null;
    }
    public final boolean hasError(){
        return this.hasErrorMessage()||this.anyFormComponentError();
    }
    public boolean isRootForm(){
        return this.findParent((Class<Form>)Form.class)==null;
    }
    public final boolean isSubmitted(){
        return this.getFlag(256);
    }
    public final void onFormSubmitted(){
        if(this.getRequest().getContainerRequest() instanceof HttpServletRequest){
            final String desiredMethod=this.getMethod();
            final String actualMethod=((HttpServletRequest)this.getRequest().getContainerRequest()).getMethod();
            if(!actualMethod.equalsIgnoreCase(desiredMethod)){
                final MethodMismatchResponse response=this.onMethodMismatch();
                switch(response){
                    case ABORT:{
                        return;
                    }
                    case CONTINUE:{
                        break;
                    }
                    default:{
                        throw new IllegalStateException("Invalid "+MethodMismatchResponse.class.getName()+" value: "+response);
                    }
                }
            }
        }
        this.onFormSubmitted(null);
    }
    protected MethodMismatchResponse onMethodMismatch(){
        return MethodMismatchResponse.CONTINUE;
    }
    public final void onFormSubmitted(IFormSubmitter submitter){
        this.markFormsSubmitted();
        if(this.handleMultiPart()){
            this.inputChanged();
            final String url=this.getRequest().getRequestParameters().getParameterValue(this.getHiddenFieldId()).toString();
            if(!Strings.isEmpty((CharSequence)url)){
                this.dispatchEvent(this.getPage(),url);
            }
            else{
                if(submitter==null){
                    submitter=this.findSubmittingButton();
                }
                if(submitter!=null&&!submitter.getDefaultFormProcessing()){
                    submitter.onSubmit();
                }
                else{
                    final Form<?> formToProcess=this.findFormToProcess(submitter);
                    formToProcess.process(submitter);
                }
            }
        }
        else if(this.hasError()){
            this.callOnError(submitter);
        }
    }
    private Form<?> findFormToProcess(final IFormSubmitter submitter){
        if(submitter==null){
            return this;
        }
        final Form<?> targetedForm=submitter.getForm();
        if(targetedForm==null){
            throw new IllegalStateException("submitting component must not return 'null' on getForm()");
        }
        final Form<?> rootForm=this.getRootForm();
        if(targetedForm==rootForm){
            return rootForm;
        }
        Form<?> formThatWantsToBeSubmitted=targetedForm;
        for(Form<?> current=targetedForm.findParent((Class<Form<?>>)Form.class);current!=null;current=current.findParent((Class<Form<?>>)Form.class)){
            if(current.wantSubmitOnNestedFormSubmit()){
                formThatWantsToBeSubmitted=current;
            }
        }
        return formThatWantsToBeSubmitted;
    }
    public boolean wantSubmitOnNestedFormSubmit(){
        return false;
    }
    public void process(final IFormSubmitter submittingComponent){
        if(!this.isEnabledInHierarchy()||!this.isVisibleInHierarchy()){
            return;
        }
        this.validate();
        if(this.hasError()){
            this.markFormComponentsInvalid();
            this.callOnError(submittingComponent);
        }
        else{
            this.markFormComponentsValid();
            this.beforeUpdateFormComponentModels();
            this.updateFormComponentModels();
            this.onValidateModelObjects();
            if(this.hasError()){
                this.callOnError(submittingComponent);
                return;
            }
            this.delegateSubmit(submittingComponent);
        }
    }
    protected void callOnError(final IFormSubmitter submitter){
        final Form<?> processingForm=this.findFormToProcess(submitter);
        if(submitter!=null){
            submitter.onError();
        }
        Visits.visitPostOrder((Object)processingForm,(IVisitor)new IVisitor<Form<?>,Void>(){
            public void component(final Form<?> form,final IVisit<Void> visit){
                if(!form.isEnabledInHierarchy()||!form.isVisibleInHierarchy()){
                    visit.dontGoDeeper();
                    return;
                }
                if(form.hasError()){
                    form.onError();
                }
            }
        },(IVisitFilter)new ClassVisitFilter(Form.class));
    }
    private void markFormsSubmitted(){
        this.setFlag(256,true);
        this.visitChildren((Class<?>)Form.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                final Form<?> form=(Form<?>)component;
                if(form.isEnabledInHierarchy()&&Form.this.isVisibleInHierarchy()){
                    form.setFlag(256,true);
                    return;
                }
                visit.dontGoDeeper();
            }
        });
    }
    public final void setDefaultButton(final IFormSubmittingComponent submittingComponent){
        if(this.isRootForm()){
            this.defaultSubmittingComponent=submittingComponent;
        }
        else{
            this.getRootForm().setDefaultButton(submittingComponent);
        }
    }
    public final void setMaxSize(final Bytes maxSize){
        this.maxSize=maxSize;
    }
    public void setMultiPart(final boolean multiPart){
        if(multiPart){
            this.multiPart|=0x1;
        }
        else{
            this.multiPart&=0xFFFFFFFE;
        }
    }
    public final Component setVersioned(final boolean isVersioned){
        super.setVersioned(isVersioned);
        this.visitFormComponents((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                formComponent.setVersioned(isVersioned);
            }
        });
        return this;
    }
    public final <R> R visitFormComponents(final IVisitor<? extends FormComponent<?>,R> visitor){
        return this.visitChildren((Class<?>)FormComponent.class,visitor);
    }
    public final <R> R visitFormComponentsPostOrder(final IVisitor<? extends FormComponent<?>,R> visitor){
        return FormComponent.visitFormComponentsPostOrder(this,visitor);
    }
    private boolean anyFormComponentError(){
        final Boolean error=this.visitChildren((Class<?>)Component.class,(org.apache.wicket.util.visit.IVisitor<Component,Boolean>)new IVisitor<Component,Boolean>(){
            public void component(final Component component,final IVisit<Boolean> visit){
                if(component.hasErrorMessage()){
                    visit.stop((Object)true);
                }
            }
        });
        return error!=null&&error;
    }
    private void dispatchEvent(final Page page,final String url){
        final String urlWoJSessionId=Strings.stripJSessionId(url);
        final Url resolved=new Url(this.getRequest().getUrl());
        resolved.resolveRelative(Url.parse(urlWoJSessionId));
        final IRequestMapper mapper=this.getApplication().getRootRequestMapper();
        final Request request=this.getRequest().cloneWithUrl(resolved);
        final IRequestHandler handler=mapper.mapRequest(request);
        if(handler!=null){
            this.getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
        }
    }
    private void inputChanged(){
        this.visitFormComponentsPostOrder((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                formComponent.inputChanged();
            }
        });
    }
    protected void appendDefaultButtonField(final MarkupStream markupStream,final ComponentTag openTag){
        final AppendingStringBuffer buffer=new AppendingStringBuffer();
        buffer.append("<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">");
        buffer.append("<input type=\"text\" autocomplete=\"off\"/>");
        final Component submittingComponent=(Component)this.defaultSubmittingComponent;
        buffer.append("<input type=\"submit\" name=\"");
        buffer.append(this.defaultSubmittingComponent.getInputName());
        buffer.append("\" onclick=\" var b=document.getElementById('");
        buffer.append(submittingComponent.getMarkupId());
        buffer.append("'); if (b!=null&amp;&amp;b.onclick!=null&amp;&amp;typeof(b.onclick) != 'undefined') {  var r = b.onclick.bind(b)(); if (r != false) b.click(); } else { b.click(); };  return false;\" ");
        buffer.append(" />");
        buffer.append("</div>");
        this.getResponse().write((CharSequence)buffer);
    }
    protected void beforeUpdateFormComponentModels(){
    }
    protected void delegateSubmit(final IFormSubmitter submittingComponent){
        final Form<?> processingForm=this.findFormToProcess(submittingComponent);
        if(submittingComponent!=null){
            submittingComponent.onSubmit();
        }
        Visits.visitPostOrder((Object)processingForm,(IVisitor)new IVisitor<Form<?>,Void>(){
            public void component(final Form<?> form,final IVisit<Void> visit){
                if(form.isEnabledInHierarchy()&&form.isVisibleInHierarchy()){
                    form.onSubmit();
                }
            }
        },(IVisitFilter)new ClassVisitFilter(Form.class));
        if(submittingComponent!=null&&submittingComponent instanceof IAfterFormSubmitter){
            ((IAfterFormSubmitter)submittingComponent).onAfterSubmit();
        }
    }
    public final String getHiddenFieldId(){
        return this.getInputNamePrefix()+this.getMarkupId()+"_hf_0";
    }
    protected String getMethod(){
        final String method=this.getMarkupAttributes().getString("method");
        return (method!=null)?method:"post";
    }
    protected boolean getStatelessHint(){
        return false;
    }
    public boolean isMultiPart(){
        if(this.multiPart!=0){
            return true;
        }
        final Boolean anyEmbeddedMultipart=this.visitChildren((Class<?>)Component.class,(org.apache.wicket.util.visit.IVisitor<Component,Boolean>)new IVisitor<Component,Boolean>(){
            public void component(final Component component,final IVisit<Boolean> visit){
                boolean isMultiPart=false;
                if(component instanceof Form){
                    final Form<?> form=(Form<?>)component;
                    if(form.isVisibleInHierarchy()&&form.isEnabledInHierarchy()){
                        isMultiPart=(form.multiPart!=0);
                    }
                }
                else if(component instanceof FormComponent){
                    final FormComponent<?> fc=(FormComponent<?>)component;
                    if(fc.isVisibleInHierarchy()&&fc.isEnabledInHierarchy()){
                        isMultiPart=fc.isMultiPart();
                    }
                }
                if(isMultiPart){
                    visit.stop((Object)true);
                }
            }
        });
        final boolean mp=Boolean.TRUE.equals(anyEmbeddedMultipart);
        if(mp){
            this.multiPart|=0x2;
        }
        return mp;
    }
    protected boolean handleMultiPart(){
        if(this.isMultiPart()){
            try{
                final ServletWebRequest request=(ServletWebRequest)this.getRequest();
                final WebRequest multipartWebRequest=request.newMultipartWebRequest(this.getMaxSize(),this.getPage().getId());
                this.getRequestCycle().setRequest((Request)multipartWebRequest);
            }
            catch(FileUploadException fux){
                final Map<String,Object> model=(Map<String,Object>)new HashMap();
                model.put("exception",fux);
                model.put("maxSize",this.getMaxSize());
                this.onFileUploadException(fux,model);
                return false;
            }
        }
        return true;
    }
    protected void onFileUploadException(final FileUploadException e,final Map<String,Object> model){
        if(e instanceof FileUploadBase.SizeLimitExceededException){
            final String defaultValue="Upload must be less than "+this.getMaxSize();
            final String msg=this.getString(this.getId()+'.'+"uploadTooLarge",Model.ofMap(model),defaultValue);
            this.error(msg);
        }
        else{
            final String defaultValue="Upload failed: "+e.getLocalizedMessage();
            final String msg=this.getString(this.getId()+'.'+"uploadFailed",Model.ofMap(model),defaultValue);
            this.error(msg);
            Form.log.warn(msg,(Throwable)e);
        }
    }
    protected void internalOnModelChanged(){
        this.visitFormComponentsPostOrder((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                if(formComponent.sameInnermostModel(Form.this)){
                    formComponent.modelChanged();
                }
            }
        });
    }
    protected final void markFormComponentsInvalid(){
        this.visitFormComponentsPostOrder((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                if(formComponent.isVisibleInHierarchy()){
                    formComponent.invalid();
                }
            }
        });
    }
    protected final void markFormComponentsValid(){
        this.internalMarkFormComponentsValid();
        this.markNestedFormComponentsValid();
    }
    private void markNestedFormComponentsValid(){
        this.visitChildren((Class<?>)Form.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Form<?>,Void>(){
            public void component(final Form<?> form,final IVisit<Void> visit){
                if(form.isEnabledInHierarchy()&&form.isVisibleInHierarchy()){
                    form.internalMarkFormComponentsValid();
                }
                else{
                    visit.dontGoDeeper();
                }
            }
        });
    }
    private void internalMarkFormComponentsValid(){
        this.visitFormComponentsPostOrder((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new IVisitor<FormComponent<?>,Void>(){
            public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
                if(formComponent.getForm()==Form.this&&formComponent.isVisibleInHierarchy()){
                    formComponent.valid();
                }
            }
        });
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        this.checkComponentTag(tag,"form");
        if(this.isRootForm()){
            final String method=this.getMethod().toLowerCase(Locale.ENGLISH);
            tag.put("method",(CharSequence)method);
            final String url=this.getActionUrl().toString();
            if(this.encodeUrlInHiddenFields()){
                final int i=url.indexOf(63);
                final String action=(i>-1)?url.substring(0,i):"";
                tag.put("action",(CharSequence)action);
            }
            else{
                tag.put("action",(CharSequence)url);
            }
            if(this.isMultiPart()){
                if("get".equalsIgnoreCase(method)){
                    Form.log.warn("Form with id '{}' is multipart. It should use method 'POST'!",this.getId());
                    tag.put("method",(CharSequence)"post".toLowerCase(Locale.ENGLISH));
                }
                tag.put("enctype",(CharSequence)"multipart/form-data");
                tag.put("accept-charset",(CharSequence)this.getApplication().getRequestCycleSettings().getResponseRequestEncoding());
            }
            else{
                final String enctype=(String)tag.getAttributes().get((Object)"enctype");
                if("multipart/form-data".equalsIgnoreCase(enctype)){
                    this.setMultiPart(true);
                }
            }
        }
        else{
            tag.setName("div");
            tag.remove("method");
            tag.remove("action");
            tag.remove("enctype");
        }
    }
    protected CharSequence getActionUrl(){
        return this.urlFor(IFormSubmitListener.INTERFACE,new PageParameters());
    }
    protected void renderPlaceholderTag(final ComponentTag tag,final Response response){
        if(this.isRootForm()){
            super.renderPlaceholderTag(tag,response);
        }
        else{
            response.write((CharSequence)"<div style=\"display:none\"");
            if(this.getOutputMarkupId()){
                response.write((CharSequence)" id=\"");
                response.write((CharSequence)this.getMarkupId());
                response.write((CharSequence)"\"");
            }
            response.write((CharSequence)"></div>");
        }
    }
    protected boolean encodeUrlInHiddenFields(){
        return "get".equalsIgnoreCase(this.getMethod());
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        if(this.isRootForm()){
            final String nameAndId=this.getHiddenFieldId();
            final AppendingStringBuffer buffer=new AppendingStringBuffer((CharSequence)"<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">").append("<input type=\"hidden\" name=\"").append(nameAndId).append("\" id=\"").append(nameAndId).append("\" />");
            if(this.encodeUrlInHiddenFields()){
                final String url=this.getActionUrl().toString();
                final int i=url.indexOf(63);
                final String[] params=((i>-1)?url.substring(i+1):url).split("&");
                this.writeParamsAsHiddenFields(params,buffer);
            }
            buffer.append("</div>");
            this.getResponse().write((CharSequence)buffer);
            if(this.defaultSubmittingComponent instanceof Component){
                final Component submittingComponent=(Component)this.defaultSubmittingComponent;
                if(submittingComponent.isVisibleInHierarchy()&&submittingComponent.isEnabledInHierarchy()){
                    this.appendDefaultButtonField(markupStream,openTag);
                }
            }
        }
        super.onComponentTagBody(markupStream,openTag);
    }
    protected void writeParamsAsHiddenFields(final String[] params,final AppendingStringBuffer buffer){
        for(final String param : params){
            final String[] pair=Strings.split(param,'=');
            buffer.append("<input type=\"hidden\" name=\"").append(this.recode(pair[0])).append("\" value=\"").append((pair.length>1)?this.recode(pair[1]):"").append("\" />");
        }
    }
    private String recode(final String s){
        final String un=UrlDecoder.QUERY_INSTANCE.decode(s,this.getRequest().getCharset());
        return Strings.escapeMarkup((CharSequence)un).toString();
    }
    protected void onDetach(){
        this.setFlag(256,false);
        super.onDetach();
    }
    protected void onError(){
    }
    protected void onBeforeRender(){
        this.multiPart&=0xFFFFFFFD;
        super.onBeforeRender();
    }
    protected void onSubmit(){
    }
    protected final void updateFormComponentModels(){
        this.internalUpdateFormComponentModels();
        this.updateNestedFormComponentModels();
    }
    private final void updateNestedFormComponentModels(){
        this.visitChildren((Class<?>)Form.class,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Form<?>,Void>(){
            public void component(final Form<?> form,final IVisit<Void> visit){
                if(form.isEnabledInHierarchy()&&form.isVisibleInHierarchy()){
                    form.internalUpdateFormComponentModels();
                }
                else{
                    visit.dontGoDeeper();
                }
            }
        });
    }
    private void internalUpdateFormComponentModels(){
        FormComponent.visitComponentsPostOrder(this,(org.apache.wicket.util.visit.IVisitor<Component,Object>)new FormModelUpdateVisitor(this));
    }
    protected final void validate(){
        if(this.isEnabledInHierarchy()&&this.isVisibleInHierarchy()){
            this.validateNestedForms();
            this.validateComponents();
            this.validateFormValidators();
            this.onValidate();
        }
    }
    protected void onValidate(){
    }
    protected void onValidateModelObjects(){
    }
    protected final void validateComponents(){
        this.visitFormComponentsPostOrder((org.apache.wicket.util.visit.IVisitor<? extends FormComponent<?>,Object>)new ValidationVisitor(){
            public void validate(final FormComponent<?> formComponent){
                final Form<?> form=formComponent.getForm();
                if(form==Form.this&&form.isEnabledInHierarchy()&&form.isVisibleInHierarchy()){
                    formComponent.validate();
                }
            }
        });
    }
    private boolean isFormComponentVisibleInPage(final FormComponent<?> fc){
        if(fc==null){
            throw new IllegalArgumentException("Argument `fc` cannot be null");
        }
        return fc.isVisibleInHierarchy();
    }
    protected final void validateFormValidator(final IFormValidator validator){
        Args.notNull((Object)validator,"validator");
        final FormComponent<?>[] dependents=validator.getDependentFormComponents();
        boolean validate=true;
        if(dependents!=null){
            for(final FormComponent<?> dependent : dependents){
                if(!dependent.isValid()){
                    validate=false;
                    break;
                }
                if(!this.isFormComponentVisibleInPage(dependent)){
                    if(Form.log.isWarnEnabled()){
                        Form.log.warn("IFormValidator in form `"+this.getPageRelativePath()+"` depends on a component that has been removed from the page or is no longer visible. "+"Offending component id `"+dependent.getId()+"`.");
                    }
                    validate=false;
                    break;
                }
            }
        }
        if(validate){
            validator.validate(this);
        }
    }
    protected final void validateFormValidators(){
        for(final Behavior behavior : this.getBehaviors()){
            if(behavior instanceof IFormValidator){
                this.validateFormValidator((IFormValidator)behavior);
            }
        }
    }
    private void validateNestedForms(){
        Visits.visitPostOrder((Object)this,(IVisitor)new IVisitor<Form<?>,Void>(){
            public void component(final Form<?> form,final IVisit<Void> visit){
                if(form==Form.this){
                    visit.stop();
                    return;
                }
                if(form.isEnabledInHierarchy()&&form.isVisibleInHierarchy()){
                    form.validateComponents();
                    form.validateFormValidators();
                    form.onValidate();
                }
            }
        },(IVisitFilter)new ClassVisitFilter(Form.class));
    }
    protected String getInputNamePrefix(){
        return "";
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
    public static Form<?> findForm(final Component component){
        return component.findParent((Class<Form<?>>)Form.class);
    }
    public void renderHead(final IHeaderResponse response){
        if(!this.isRootForm()&&this.isMultiPart()){
            this.registerJavaScriptNamespaces(response);
            response.renderJavaScript((CharSequence)("Wicket.Forms[\""+this.getMarkupId()+"\"]={multipart:true};"),Form.class.getName()+'.'+this.getMarkupId()+".metadata");
        }
    }
    protected void registerJavaScriptNamespaces(final IHeaderResponse response){
        response.renderJavaScript((CharSequence)"if (typeof(Wicket)=='undefined') { Wicket={}; } if (typeof(Wicket.Forms)=='undefined') { Wicket.Forms={}; }",Form.class.getName());
    }
    public static String getRootFormRelativeId(final Component component){
        String id=component.getId();
        final PrependingStringBuffer inputName=new PrependingStringBuffer(id.length());
        Component c=component;
        while(true){
            inputName.prepend(id);
            c=c.getParent();
            if(c==null||(c instanceof Form&&((Form)c).isRootForm())||c instanceof Page){
                break;
            }
            inputName.prepend(':');
            id=c.getId();
        }
        if("submit".equals(inputName.toString())){
            inputName.prepend(':');
        }
        return inputName.toString();
    }
    static{
        log=LoggerFactory.getLogger(Form.class);
    }
    public abstract static class ValidationVisitor implements IVisitor<FormComponent<?>,Void>{
        public void component(final FormComponent<?> formComponent,final IVisit<Void> visit){
            final Form<?> form=formComponent.getForm();
            if(!form.isVisibleInHierarchy()||!form.isEnabledInHierarchy()){
                visit.dontGoDeeper();
                return;
            }
            if(formComponent.isVisibleInHierarchy()&&formComponent.isValid()&&formComponent.isEnabledInHierarchy()){
                this.validate(formComponent);
            }
            if(!formComponent.processChildren()){
                visit.dontGoDeeper();
            }
        }
        public abstract void validate(final FormComponent<?> p0);
    }
    private static class FormModelUpdateVisitor implements IVisitor<Component,Void>{
        private final Form<?> formFilter;
        public FormModelUpdateVisitor(final Form<?> formFilter){
            super();
            this.formFilter=formFilter;
        }
        public void component(final Component component,final IVisit<Void> visit){
            if(component instanceof IFormModelUpdateListener){
                final Form<?> form=Form.findForm(component);
                if(form!=null&&(this.formFilter==null||this.formFilter==form)&&form.isEnabledInHierarchy()&&component.isVisibleInHierarchy()&&component.isEnabledInHierarchy()){
                    ((IFormModelUpdateListener)component).updateModel();
                }
            }
        }
    }
    public enum MethodMismatchResponse{
        CONTINUE,ABORT;
    }
}
