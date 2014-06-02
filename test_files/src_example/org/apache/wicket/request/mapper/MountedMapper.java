package org.apache.wicket.request.mapper;

import org.apache.wicket.util.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.mapper.parameter.*;
import java.util.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.mapper.info.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;

public class MountedMapper extends AbstractBookmarkableMapper{
    private final IPageParametersEncoder pageParametersEncoder;
    private final List<MountPathSegment> pathSegments;
    private final String[] mountSegments;
    private final ClassProvider<? extends IRequestablePage> pageClassProvider;
    public MountedMapper(final String mountPath,final Class<? extends IRequestablePage> pageClass){
        this(mountPath,pageClass,(IPageParametersEncoder)new PageParametersEncoder());
    }
    public MountedMapper(final String mountPath,final ClassProvider<? extends IRequestablePage> pageClassProvider){
        this(mountPath,pageClassProvider,(IPageParametersEncoder)new PageParametersEncoder());
    }
    public MountedMapper(final String mountPath,final Class<? extends IRequestablePage> pageClass,final IPageParametersEncoder pageParametersEncoder){
        this(mountPath,(ClassProvider<? extends IRequestablePage>)ClassProvider.of((Class)pageClass),pageParametersEncoder);
    }
    public MountedMapper(final String mountPath,final ClassProvider<? extends IRequestablePage> pageClassProvider,final IPageParametersEncoder pageParametersEncoder){
        super();
        Args.notEmpty((CharSequence)mountPath,"mountPath");
        Args.notNull((Object)pageClassProvider,"pageClassProvider");
        Args.notNull((Object)pageParametersEncoder,"pageParametersEncoder");
        this.pageParametersEncoder=pageParametersEncoder;
        this.pageClassProvider=pageClassProvider;
        this.mountSegments=this.getMountSegments(mountPath);
        this.pathSegments=this.getPathSegments(this.mountSegments);
    }
    private List<MountPathSegment> getPathSegments(final String[] segments){
        final List<MountPathSegment> ret=(List<MountPathSegment>)new ArrayList();
        int segmentIndex=0;
        MountPathSegment curPathSegment=new MountPathSegment(segmentIndex);
        ret.add(curPathSegment);
        for(final String curSegment : segments){
            if(this.isFixedSegment(curSegment)){
                curPathSegment.setFixedPart(curSegment);
                curPathSegment=new MountPathSegment(segmentIndex+1);
                ret.add(curPathSegment);
            }
            else if(this.getPlaceholder(curSegment)!=null){
                curPathSegment.addRequiredParameter();
            }
            else{
                curPathSegment.addOptionalParameter();
            }
            ++segmentIndex;
        }
        return ret;
    }
    private boolean isFixedSegment(final String segment){
        return this.getOptionalPlaceholder(segment)==null&&this.getPlaceholder(segment)==null;
    }
    protected UrlInfo parseRequest(final Request request){
        final Url url=request.getUrl();
        if(this.redirectFromHomePage()&&this.checkHomePage(url)){
            return new UrlInfo(null,this.getContext().getHomePageClass(),this.newPageParameters());
        }
        if(this.urlStartsWith(url,this.mountSegments)){
            final PageComponentInfo info=this.getPageComponentInfo(url);
            final Class<? extends IRequestablePage> pageClass=this.getPageClass();
            final PageParameters pageParameters=this.extractPageParameters(request,url);
            return new UrlInfo(info,pageClass,pageParameters);
        }
        return null;
    }
    private PageParameters extractPageParameters(final Request request,final Url url){
        final int[] matchedParameters=this.getMatchedSegmentSizes(url);
        int total=0;
        for(final int curMatchSize : matchedParameters){
            total+=curMatchSize;
        }
        PageParameters pageParameters=this.extractPageParameters(request,total,this.pageParametersEncoder);
        int skippedParameters=0;
        for(int pathSegmentIndex=0;pathSegmentIndex<this.pathSegments.size();++pathSegmentIndex){
            final MountPathSegment curPathSegment=(MountPathSegment)this.pathSegments.get(pathSegmentIndex);
            final int matchSize=matchedParameters[pathSegmentIndex]-curPathSegment.getFixedPartSize();
            int optionalParameterMatch=matchSize-curPathSegment.getMinParameters();
            for(int matchSegment=0;matchSegment<matchSize;++matchSegment){
                if(pageParameters==null){
                    pageParameters=new PageParameters();
                }
                final int curSegmentIndex=matchSegment+curPathSegment.getSegmentIndex();
                final String curSegment=this.mountSegments[curSegmentIndex];
                final String placeholder=this.getPlaceholder(curSegment);
                final String optionalPlaceholder=this.getOptionalPlaceholder(curSegment);
                if(placeholder!=null){
                    pageParameters.add(placeholder,url.getSegments().get(curSegmentIndex-skippedParameters));
                }
                else if(optionalPlaceholder!=null&&optionalParameterMatch>0){
                    pageParameters.add(optionalPlaceholder,url.getSegments().get(curSegmentIndex-skippedParameters));
                    --optionalParameterMatch;
                }
            }
            skippedParameters+=curPathSegment.getMaxParameters()-matchSize;
        }
        return pageParameters;
    }
    protected boolean urlStartsWith(final Url url,final String... segments){
        return url!=null&&this.getMatchedSegmentSizes(url)!=null;
    }
    private int[] getMatchedSegmentSizes(final Url url){
        final int[] ret=new int[this.pathSegments.size()];
        int segmentIndex=0;
        int pathSegmentIndex=0;
        for(final MountPathSegment curPathSegment : this.pathSegments.subList(0,this.pathSegments.size()-1)){
            boolean foundFixedPart=false;
            segmentIndex+=curPathSegment.getMinParameters();
            final int max=Math.min(curPathSegment.getOptionalParameters()+1,url.getSegments().size()-segmentIndex);
            for(int count=max-1;count>=0;--count){
                if(((String)url.getSegments().get(segmentIndex+count)).equals(curPathSegment.getFixedPart())){
                    foundFixedPart=true;
                    segmentIndex+=count+1;
                    ret[pathSegmentIndex]=count+curPathSegment.getMinParameters()+1;
                    break;
                }
            }
            if(!foundFixedPart){
                return null;
            }
            ++pathSegmentIndex;
        }
        final MountPathSegment lastSegment=(MountPathSegment)this.pathSegments.get(this.pathSegments.size()-1);
        segmentIndex+=lastSegment.getMinParameters();
        if(segmentIndex>url.getSegments().size()){
            return null;
        }
        ret[pathSegmentIndex]=Math.min(lastSegment.getMaxParameters(),url.getSegments().size()-segmentIndex+lastSegment.getMinParameters());
        return ret;
    }
    protected PageParameters newPageParameters(){
        return new PageParameters();
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        Url url=super.mapHandler(requestHandler);
        if(url==null&&requestHandler instanceof ListenerInterfaceRequestHandler&&this.getRecreateMountedPagesAfterExpiry()){
            final ListenerInterfaceRequestHandler handler=(ListenerInterfaceRequestHandler)requestHandler;
            final IRequestablePage page=handler.getPage();
            if(this.checkPageInstance(page)){
                final String componentPath=handler.getComponentPath();
                final RequestListenerInterface listenerInterface=handler.getListenerInterface();
                Integer renderCount=null;
                if(listenerInterface.isIncludeRenderCount()){
                    renderCount=page.getRenderCount();
                }
                final PageInfo pageInfo=this.getPageInfo(handler);
                final ComponentInfo componentInfo=new ComponentInfo(renderCount,this.requestListenerInterfaceToString(listenerInterface),componentPath,handler.getBehaviorIndex());
                final PageComponentInfo pageComponentInfo=new PageComponentInfo(pageInfo,componentInfo);
                final PageParameters parameters=new PageParameters(page.getPageParameters());
                final UrlInfo urlInfo=new UrlInfo(pageComponentInfo,(Class<? extends IRequestablePage>)page.getClass(),parameters.mergeWith(handler.getPageParameters()));
                url=this.buildUrl(urlInfo);
            }
        }
        return url;
    }
    boolean getRecreateMountedPagesAfterExpiry(){
        return Application.get().getPageSettings().getRecreateMountedPagesAfterExpiry();
    }
    protected Url buildUrl(final UrlInfo info){
        final Url url=new Url();
        for(final String s : this.mountSegments){
            url.getSegments().add(s);
        }
        this.encodePageComponentInfo(url,info.getPageComponentInfo());
        final PageParameters copy=new PageParameters(info.getPageParameters());
        int dropped=0;
        for(int i=0;i<this.mountSegments.length;++i){
            final String placeholder=this.getPlaceholder(this.mountSegments[i]);
            final String optionalPlaceholder=this.getOptionalPlaceholder(this.mountSegments[i]);
            if(placeholder!=null){
                url.getSegments().set(i-dropped,copy.get(placeholder).toString(""));
                copy.remove(placeholder);
            }
            else if(optionalPlaceholder!=null){
                if(copy.getNamedKeys().contains(optionalPlaceholder)){
                    url.getSegments().set(i-dropped,copy.get(optionalPlaceholder).toString(""));
                    copy.remove(optionalPlaceholder);
                }
                else{
                    url.getSegments().remove(i-dropped);
                    ++dropped;
                }
            }
        }
        return this.encodePageParameters(url,copy,this.pageParametersEncoder);
    }
    private boolean checkHomePage(final Url url){
        return url.getSegments().isEmpty()&&url.getQueryParameters().isEmpty()&&this.getPageClass().equals(this.getContext().getHomePageClass())&&this.redirectFromHomePage();
    }
    protected boolean redirectFromHomePage(){
        return true;
    }
    protected boolean pageMustHaveBeenCreatedBookmarkable(){
        return false;
    }
    public int getCompatibilityScore(final Request request){
        if(this.urlStartsWith(request.getUrl(),this.mountSegments)){
            return this.mountSegments.length;
        }
        return 0;
    }
    protected boolean checkPageClass(final Class<? extends IRequestablePage> pageClass){
        return pageClass.equals(this.getPageClass());
    }
    private Class<? extends IRequestablePage> getPageClass(){
        return (Class<? extends IRequestablePage>)this.pageClassProvider.get();
    }
    public String toString(){
        return "MountedMapper [mountSegments="+Strings.join("/",this.mountSegments)+"]";
    }
    private static class MountPathSegment{
        private int segmentIndex;
        private String fixedPart;
        private int minParameters;
        private int optionalParameters;
        public MountPathSegment(final int segmentIndex){
            super();
            this.segmentIndex=segmentIndex;
        }
        public void setFixedPart(final String fixedPart){
            this.fixedPart=fixedPart;
        }
        public void addRequiredParameter(){
            ++this.minParameters;
        }
        public void addOptionalParameter(){
            ++this.optionalParameters;
        }
        public int getSegmentIndex(){
            return this.segmentIndex;
        }
        public String getFixedPart(){
            return this.fixedPart;
        }
        public int getMinParameters(){
            return this.minParameters;
        }
        public int getOptionalParameters(){
            return this.optionalParameters;
        }
        public int getMaxParameters(){
            return this.getOptionalParameters()+this.getMinParameters();
        }
        public int getFixedPartSize(){
            return (this.getFixedPart()!=null)?1:0;
        }
        public String toString(){
            return "("+this.getSegmentIndex()+") "+this.getMinParameters()+"-"+this.getMaxParameters()+" "+((this.getFixedPart()==null)?"(end)":this.getFixedPart());
        }
    }
}
