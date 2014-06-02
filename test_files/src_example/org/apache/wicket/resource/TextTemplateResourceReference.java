package org.apache.wicket.resource;

import org.apache.wicket.model.*;
import org.apache.wicket.util.template.*;
import java.util.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.*;
import org.apache.wicket.request.resource.*;

public class TextTemplateResourceReference extends ResourceReference implements IClusterable{
    private static final long serialVersionUID=1L;
    private final TextTemplate textTemplate;
    private final IModel<Map<String,Object>> variablesModel;
    private final ResourceStreamResource resource;
    public TextTemplateResourceReference(final Class<?> scope,final String fileName,final IModel<Map<String,Object>> variablesModel){
        this(scope,fileName,"text",PackageTextTemplate.DEFAULT_ENCODING,variablesModel);
    }
    public TextTemplateResourceReference(final Class<?> scope,final String fileName,final String contentType,final IModel<Map<String,Object>> variablesModel){
        this(scope,fileName,contentType,PackageTextTemplate.DEFAULT_ENCODING,variablesModel);
    }
    public TextTemplateResourceReference(final Class<?> scope,final String fileName,final String contentType,final String encoding,final IModel<Map<String,Object>> variablesModel){
        this(scope,fileName,contentType,encoding,variablesModel,null,null,null);
    }
    public TextTemplateResourceReference(final Class<?> scope,final String fileName,final String contentType,final String encoding,final IModel<Map<String,Object>> variablesModel,final Locale locale,final String style,final String variation){
        super(scope,fileName,locale,style,variation);
        this.textTemplate=new PackageTextTemplate(scope,fileName,contentType,encoding);
        this.variablesModel=variablesModel;
        (this.resource=new ResourceStreamResource(null){
            protected IResourceStream getResourceStream(){
                final IModel<Map<String,Object>> variables=TextTemplateResourceReference.this.variablesModel;
                final String stringValue=TextTemplateResourceReference.this.textTemplate.asString(variables.getObject());
                variables.detach();
                final StringResourceStream resourceStream=new StringResourceStream((CharSequence)stringValue,TextTemplateResourceReference.this.textTemplate.getContentType());
                resourceStream.setLastModified(Time.now());
                return (IResourceStream)resourceStream;
            }
        }).setCacheDuration(Duration.NONE);
        if(Application.exists()){
            final ResourceReferenceRegistry resourceReferenceRegistry=Application.get().getResourceReferenceRegistry();
            resourceReferenceRegistry.unregisterResourceReference(this.getKey());
            resourceReferenceRegistry.registerResourceReference(this);
        }
    }
    public IResource getResource(){
        return this.resource;
    }
}
