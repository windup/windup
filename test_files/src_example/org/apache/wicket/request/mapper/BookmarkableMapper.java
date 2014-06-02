package org.apache.wicket.request.mapper;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.*;
import org.apache.wicket.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.info.*;
import org.apache.wicket.request.mapper.parameter.*;

public class BookmarkableMapper extends AbstractBookmarkableMapper{
    private final IPageParametersEncoder pageParametersEncoder;
    public BookmarkableMapper(final IPageParametersEncoder pageParametersEncoder){
        super();
        Args.notNull((Object)pageParametersEncoder,"pageParametersEncoder");
        this.pageParametersEncoder=pageParametersEncoder;
    }
    public BookmarkableMapper(){
        this((IPageParametersEncoder)new PageParametersEncoder());
    }
    protected Url buildUrl(final UrlInfo info){
        final Url url=new Url();
        url.getSegments().add(this.getContext().getNamespace());
        url.getSegments().add(this.getContext().getBookmarkableIdentifier());
        url.getSegments().add(info.getPageClass().getName());
        this.encodePageComponentInfo(url,info.getPageComponentInfo());
        return this.encodePageParameters(url,info.getPageParameters(),this.pageParametersEncoder);
    }
    protected UrlInfo parseRequest(final Request request){
        if(Application.exists()&&Application.get().getSecuritySettings().getEnforceMounts()){
            return null;
        }
        final Url url=request.getUrl();
        if(this.matches(url)){
            final PageComponentInfo info=this.getPageComponentInfo(url);
            final String className=(String)url.getSegments().get(2);
            final Class<? extends IRequestablePage> pageClass=this.getPageClass(className);
            if(pageClass!=null&&IRequestablePage.class.isAssignableFrom(pageClass)){
                final PageParameters pageParameters=this.extractPageParameters(request,3,this.pageParametersEncoder);
                return new UrlInfo(info,pageClass,pageParameters);
            }
        }
        return null;
    }
    protected boolean pageMustHaveBeenCreatedBookmarkable(){
        return true;
    }
    public int getCompatibilityScore(final Request request){
        int score=0;
        final Url url=request.getUrl();
        if(this.matches(url)){
            score=Integer.MAX_VALUE;
        }
        return score;
    }
    private boolean matches(final Url url){
        return url.getSegments().size()>=3&&this.urlStartsWith(url,new String[] { this.getContext().getNamespace(),this.getContext().getBookmarkableIdentifier() });
    }
}
