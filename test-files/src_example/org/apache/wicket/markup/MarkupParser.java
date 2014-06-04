package org.apache.wicket.markup;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.parser.filter.*;
import java.util.*;
import org.apache.wicket.util.lang.*;

public class MarkupParser extends AbstractMarkupParser{
    public static final String WICKET="wicket";
    public MarkupParser(final MarkupResourceStream resource){
        super(resource);
    }
    public MarkupParser(final String markup){
        super(markup);
    }
    public MarkupParser(final IXmlPullParser xmlParser,final MarkupResourceStream resource){
        super(xmlParser,resource);
    }
    public MarkupFilterList getMarkupFilters(){
        return (MarkupFilterList)super.getMarkupFilters();
    }
    public final boolean add(final IMarkupFilter filter){
        return this.getMarkupFilters().add(filter);
    }
    public final boolean add(final IMarkupFilter filter,final Class<? extends IMarkupFilter> beforeFilter){
        return this.getMarkupFilters().add(filter,beforeFilter);
    }
    protected IMarkupFilter onAppendMarkupFilter(final IMarkupFilter filter){
        return filter;
    }
    protected MarkupFilterList initializeMarkupFilters(final Markup markup){
        final MarkupFilterList filters=new MarkupFilterList();
        final MarkupResourceStream markupResourceStream=markup.getMarkupResourceStream();
        filters.add(new WicketTagIdentifier(markupResourceStream));
        filters.add(new HtmlHandler());
        filters.add(new WicketRemoveTagHandler());
        filters.add(new WicketLinkTagHandler());
        filters.add(new AutoLabelTagHandler());
        filters.add(new WicketNamespaceHandler(markupResourceStream));
        filters.add(new WicketMessageTagHandler(markupResourceStream));
        if(markupResourceStream!=null&&markupResourceStream.getResource()!=null){
            final ContainerInfo containerInfo=markupResourceStream.getContainerInfo();
            if(containerInfo!=null){
                if(Page.class.isAssignableFrom(containerInfo.getContainerClass())){
                    filters.add(new HtmlHeaderSectionHandler(markup));
                }
                filters.add(new HeadForceTagIdHandler(containerInfo.getContainerClass()));
            }
        }
        filters.add(new OpenCloseTagExpander());
        filters.add(new RelativePathPrefixHandler(markupResourceStream));
        filters.add(new EnclosureHandler());
        filters.add(new InlineEnclosureHandler());
        filters.add(new StyleAndScriptIdentifier(markup),(Class<? extends IMarkupFilter>)StyleAndScriptIdentifier.class);
        filters.add(new ConditionalCommentFilter());
        return filters;
    }
    public class MarkupFilterList extends ArrayList<IMarkupFilter>{
        private static final long serialVersionUID=1L;
        public boolean add(final IMarkupFilter filter){
            return this.add(filter,(Class<? extends IMarkupFilter>)RelativePathPrefixHandler.class);
        }
        public boolean add(IMarkupFilter filter,final Class<? extends IMarkupFilter> beforeFilter){
            filter=this.onAdd(filter);
            if(filter==null){
                return false;
            }
            final int index=this.firstIndexOfClass(beforeFilter);
            if(index<0){
                return super.add(filter);
            }
            super.add(index,filter);
            return true;
        }
        private int firstIndexOfClass(final Class<? extends IMarkupFilter> filterClass){
            int result=-1;
            if(filterClass!=null){
                for(int size=this.size(),index=0;index<size;++index){
                    final Class<? extends IMarkupFilter> currentFilterClass=(Class<? extends IMarkupFilter>)((IMarkupFilter)this.get(index)).getClass();
                    if(Objects.equal((Object)filterClass,(Object)currentFilterClass)){
                        result=index;
                        break;
                    }
                }
            }
            return result;
        }
        protected IMarkupFilter onAdd(final IMarkupFilter filter){
            return MarkupParser.this.onAppendMarkupFilter(filter);
        }
    }
}
