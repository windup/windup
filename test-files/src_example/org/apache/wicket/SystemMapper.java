package org.apache.wicket;

import org.apache.wicket.request.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.*;
import org.apache.wicket.request.mapper.*;
import org.apache.wicket.request.resource.caching.*;

public class SystemMapper extends CompoundRequestMapper{
    private final Application application;
    public SystemMapper(final Application application){
        super();
        this.application=application;
        this.add((IRequestMapper)new PageInstanceMapper());
        this.add((IRequestMapper)new BookmarkableMapper());
        this.add((IRequestMapper)new HomePageMapper(new HomePageProvider<IRequestablePage>(application)));
        this.add((IRequestMapper)new ResourceReferenceMapper((IPageParametersEncoder)new PageParametersEncoder(),(IProvider<String>)new ParentFolderPlaceholderProvider(application),this.getResourceCachingStrategy()));
        this.add(RestartResponseAtInterceptPageException.MAPPER);
        this.add((IRequestMapper)new BufferedResponseMapper());
    }
    private IProvider<IResourceCachingStrategy> getResourceCachingStrategy(){
        return (IProvider<IResourceCachingStrategy>)new IProvider<IResourceCachingStrategy>(){
            public IResourceCachingStrategy get(){
                return SystemMapper.this.application.getResourceSettings().getCachingStrategy();
            }
        };
    }
    private static class ParentFolderPlaceholderProvider implements IProvider<String>{
        private final Application application;
        public ParentFolderPlaceholderProvider(final Application application){
            super();
            this.application=application;
        }
        public String get(){
            return this.application.getResourceSettings().getParentFolderPlaceholder();
        }
    }
    private static class HomePageProvider<C extends IRequestablePage> extends ClassProvider<C>{
        private final Application application;
        private HomePageProvider(final Application application){
            super((Class)null);
            this.application=application;
        }
        public Class<C> get(){
            final Class<C> homePage=(Class<C>)this.application.getHomePage();
            return homePage;
        }
    }
}
