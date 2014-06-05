package org.apache.wicket.markup.html.form.upload;

import org.apache.wicket.*;
import org.apache.wicket.markup.html.basic.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.upload.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.request.resource.*;
import java.util.*;
import org.apache.wicket.model.*;

public class MultiFileUploadField extends FormComponentPanel<Collection<FileUpload>>{
    private static final long serialVersionUID=1L;
    public static final int UNLIMITED=-1;
    public static final String RESOURCE_LIMITED="org.apache.wicket.mfu.caption.limited";
    public static final String RESOURCE_UNLIMITED="org.apache.wicket.mfu.caption.unlimited";
    private static final String NAME_ATTR="name";
    private static final String MAGIC_SEPARATOR="_mf_";
    private static final ResourceReference JS;
    private final WebComponent upload;
    private final WebMarkupContainer container;
    private final int max;
    private transient String[] inputArrayCache;
    public MultiFileUploadField(final String id){
        this(id,null,-1);
    }
    public MultiFileUploadField(final String id,final int max){
        this(id,null,max);
    }
    public MultiFileUploadField(final String id,final IModel<? extends Collection<FileUpload>> model){
        this(id,model,-1);
    }
    public MultiFileUploadField(final String id,final IModel<? extends Collection<FileUpload>> model,final int max){
        super(id,model);
        this.inputArrayCache=null;
        this.max=max;
        (this.upload=new WebComponent("upload")).setOutputMarkupId(true);
        this.add(this.upload);
        (this.container=new WebMarkupContainer("container")).setOutputMarkupId(true);
        this.add(this.container);
        this.container.add(new Label("caption",new CaptionModel()));
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(tag.getAttributes().containsKey((Object)"name")){
            tag.getAttributes().remove((Object)"name");
        }
    }
    protected void onBeforeRender(){
        super.onBeforeRender();
        final Form<?> form=this.findParent((Class<Form<?>>)Form.class);
        if(form==null){
            throw new IllegalStateException("Component "+this.getClass().getName()+" must have a "+Form.class.getName()+" component above in the hierarchy");
        }
    }
    public boolean isMultiPart(){
        return true;
    }
    public void renderHead(final IHeaderResponse response){
        response.renderJavaScriptReference(MultiFileUploadField.JS);
        response.renderOnDomReadyJavaScript("new MultiSelector('"+this.getInputName()+"', document.getElementById('"+this.container.getMarkupId()+"'), "+this.max+",'"+this.getString("org.apache.wicket.mfu.delete")+"').addElement(document.getElementById('"+this.upload.getMarkupId()+"'));");
    }
    public String[] getInputAsArray(){
        if(this.inputArrayCache==null){
            ArrayList<String> names=null;
            final Request request=this.getRequest();
            if(request instanceof IMultipartWebRequest){
                final Map<String,List<FileItem>> itemNameToItem=((IMultipartWebRequest)request).getFiles();
                for(final Map.Entry<String,List<FileItem>> entry : itemNameToItem.entrySet()){
                    final String name=(String)entry.getKey();
                    final List<FileItem> fileItems=(List<FileItem>)entry.getValue();
                    if(!Strings.isEmpty((CharSequence)name)&&name.startsWith(this.getInputName()+"_mf_")&&!fileItems.isEmpty()&&!Strings.isEmpty((CharSequence)((FileItem)fileItems.get(0)).getName())){
                        names=(ArrayList<String>)((names!=null)?names:new ArrayList());
                        names.add(name);
                    }
                }
            }
            if(names!=null){
                this.inputArrayCache=(String[])names.toArray(new String[names.size()]);
            }
        }
        return this.inputArrayCache;
    }
    protected Collection<FileUpload> convertValue(final String[] value) throws ConversionException{
        Collection<FileUpload> uploads=null;
        final String[] filenames=this.getInputAsArray();
        if(filenames!=null){
            final IMultipartWebRequest request=(IMultipartWebRequest)this.getRequest();
            uploads=(Collection<FileUpload>)new ArrayList(filenames.length);
            for(final String filename : filenames){
                final List<FileItem> fileItems=request.getFile(filename);
                for(final FileItem fileItem : fileItems){
                    uploads.add(new FileUpload(fileItem));
                }
            }
        }
        return uploads;
    }
    public void updateModel(){
        FormComponent.updateCollectionModel((FormComponent<Collection<Object>>)this);
    }
    protected void onDetach(){
        final Collection<FileUpload> uploads=(Collection<FileUpload>)((FormComponent<Collection>)this).getConvertedInput();
        if(uploads!=null){
            for(final FileUpload upload : uploads){
                upload.closeStreams();
            }
        }
        this.inputArrayCache=null;
        final Collection<FileUpload> modelObject=(Collection<FileUpload>)((FormComponent<Collection>)this).getModelObject();
        if(modelObject!=null){
            modelObject.clear();
        }
        super.onDetach();
    }
    static{
        JS=new JavaScriptResourceReference((Class<?>)MultiFileUploadField.class,"MultiFileUploadField.js");
    }
    private class CaptionModel extends AbstractReadOnlyModel<String>{
        private static final long serialVersionUID=1L;
        public String getObject(){
            if(MultiFileUploadField.this.max==-1){
                return MultiFileUploadField.this.getString("org.apache.wicket.mfu.caption.unlimited");
            }
            final HashMap<String,Object> vars=(HashMap<String,Object>)new HashMap(1);
            vars.put("max",MultiFileUploadField.this.max);
            return MultiFileUploadField.this.getString("org.apache.wicket.mfu.caption.limited",new Model<Object>(vars));
        }
    }
}
