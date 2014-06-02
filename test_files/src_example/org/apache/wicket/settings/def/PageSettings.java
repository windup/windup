package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import java.util.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.util.lang.*;

public class PageSettings implements IPageSettings{
    private final List<IComponentResolver> componentResolvers;
    private boolean versionPagesByDefault;
    private boolean recreateMountedPagesAfterExpiry;
    public PageSettings(){
        super();
        this.componentResolvers=(List<IComponentResolver>)Generics.newArrayList();
        this.versionPagesByDefault=true;
        this.recreateMountedPagesAfterExpiry=true;
    }
    public void addComponentResolver(final IComponentResolver resolver){
        this.componentResolvers.add(resolver);
    }
    public List<IComponentResolver> getComponentResolvers(){
        return this.componentResolvers;
    }
    public boolean getVersionPagesByDefault(){
        return this.versionPagesByDefault;
    }
    public void setVersionPagesByDefault(final boolean pagesVersionedByDefault){
        this.versionPagesByDefault=pagesVersionedByDefault;
    }
    public boolean getRecreateMountedPagesAfterExpiry(){
        return this.recreateMountedPagesAfterExpiry;
    }
    public void setRecreateMountedPagesAfterExpiry(final boolean recreateMountedPagesAfterExpiry){
        this.recreateMountedPagesAfterExpiry=recreateMountedPagesAfterExpiry;
    }
}
