package org.apache.wicket.markup.loader;

import org.apache.wicket.*;
import java.io.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.markup.*;

public class InheritedMarkupMarkupLoader implements IMarkupLoader{
    public final Markup loadMarkup(final MarkupContainer container,final MarkupResourceStream markupResourceStream,final IMarkupLoader baseLoader,final boolean enforceReload) throws IOException,ResourceStreamNotFoundException{
        final Markup markup=baseLoader.loadMarkup(container,markupResourceStream,null,enforceReload);
        final int extendIndex=this.requiresBaseMarkup(markup);
        if(extendIndex==-1){
            return markup;
        }
        final Markup baseMarkup=this.getBaseMarkup(container,markup,enforceReload);
        if(baseMarkup==null||baseMarkup==Markup.NO_MARKUP){
            throw new MarkupNotFoundException("Base markup of inherited markup not found. Component class: "+markup.getMarkupResourceStream().getContainerInfo().getContainerClass().getName()+". Enable debug messages for org.apache.wicket.util.resource.locator.ResourceStreamLocator to get a list of all filenames tried.");
        }
        return new MergedMarkup(markup,baseMarkup,extendIndex);
    }
    private Markup getBaseMarkup(final MarkupContainer container,final Markup markup,final boolean enforceReload){
        final Class<?> location=(Class<?>)markup.getMarkupResourceStream().getMarkupClass().getSuperclass();
        return MarkupFactory.get().getMarkup(container,location,enforceReload);
    }
    private int requiresBaseMarkup(final IMarkupFragment markup){
        for(int i=0;i<markup.size();++i){
            if(TagUtils.isExtendTag(markup,i)){
                return i;
            }
        }
        return -1;
    }
}
