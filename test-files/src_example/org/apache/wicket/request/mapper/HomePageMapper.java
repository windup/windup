package org.apache.wicket.request.mapper;

import org.apache.wicket.request.component.*;
import org.apache.wicket.util.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;

public class HomePageMapper extends MountedMapper{
    public HomePageMapper(final Class<? extends IRequestablePage> pageClass){
        super("/",pageClass);
    }
    public HomePageMapper(final ClassProvider<? extends IRequestablePage> pageClassProvider){
        super("/",pageClassProvider);
    }
    public HomePageMapper(final Class<? extends IRequestablePage> pageClass,final IPageParametersEncoder pageParametersEncoder){
        super("/",pageClass,pageParametersEncoder);
    }
    public HomePageMapper(final ClassProvider<? extends IRequestablePage> pageClassProvider,final IPageParametersEncoder pageParametersEncoder){
        super("/",pageClassProvider,pageParametersEncoder);
    }
    protected UrlInfo parseRequest(final Request request){
        final Url url=request.getUrl().canonical();
        if(url.getSegments().size()>0){
            return null;
        }
        return super.parseRequest(request);
    }
    public int getCompatibilityScore(final Request request){
        return -2147483647;
    }
}
